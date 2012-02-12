package pt.ipb.nmsfs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.VariableBinding;

import pt.ipb.marser.MibOps;
import fuse.Errno;
import fuse.FuseDirFiller;
import fuse.FuseException;
import fuse.FuseFtypeConstants;
import fuse.FuseGetattrSetter;
import fuse.FuseMount;
import fuse.FuseOpenSetter;
import fuse.compat.FuseStat;

public class AgentFS extends AbstractFS {
	private static final Log log = LogFactory.getLog(AgentFS.class);
	 public static final String ROOTDIR = "/";

	MibOps mibOps;
	AgentTree tree;

	SnmpBackend backend;

	public AgentFS(MibOps mibOps) {
		this.mibOps = mibOps;
		try {
			this.backend = new SnmpBackend("bart", "bartsimpson");
			this.backend.open();
			tree = new AgentTree(backend, mibOps);
			resolveAgent();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void resolveAgent() throws IOException {
		log.info("resolving agent...");
		OID oid = new OID("1");
		while (true) {
			VariableBinding varBind = backend.getNext(oid);
			if (varBind.isException()) {
				break;
			}
			tree.mkdirs(varBind);
			oid = varBind.getOid();
		}
		log.info("done!");
	}

	@Override
	public int getattr(String path, FuseGetattrSetter setter) throws FuseException {
		log.debug("getattr: " + path);
		Node n = resolvePath(path);
		FuseStat fs = n.getStat();
		setter.set(fs.inode, fs.mode, fs.nlink, fs.uid, fs.gid, 0, fs.size, fs.blocks, fs.atime, fs.mtime, fs.ctime);
		return 0;
	}

	protected String toOIDString(String path) {
		Pattern pattern = Pattern.compile("/");
		Matcher matcher = pattern.matcher(path);
		return matcher.replaceAll(".");
	}

	@Override
	public int getdir(String path, FuseDirFiller filler) throws FuseException {
		log.debug("getdir: " + path);
		Node node = resolvePath(path);

		if (!(node instanceof DirNode))
			return Errno.ENOTDIR;

		for (Node n : ((DirNode) node).children()) {
			if (n instanceof SymlinkNode) {
				String name = n.getName();
				filler.add(name, name.hashCode(), FuseFtypeConstants.TYPE_SYMLINK);
			} else if (n instanceof FileNode) {
				String name = n.getName();
				filler.add(name, name.hashCode(), FuseFtypeConstants.TYPE_FILE);
			} else {
				String name = n.getName();
				filler.add(name, name.hashCode(), FuseFtypeConstants.TYPE_DIR);
			}
		}
		return 0;
	}

	@Override
	public int readlink(String path, CharBuffer link) throws FuseException {
		log.debug("readlink: " + path + "; link: " + link);
		Node n = resolvePath(path);
		if (n instanceof SymlinkNode) {
			link.append(Integer.toString(n.getOid().last()));
			return 0;
		}
		return Errno.ENOLINK;
	}

	protected String oidToPath(OID oid) {
		StringBuilder str = new StringBuilder();
		for (int i : oid.getValue()) {
			str.append(ROOTDIR);
			str.append(i);
		}
		return str.toString();
	}

	@Override
	public int open(String path, int flags, FuseOpenSetter arg2) throws FuseException {
		log.debug("open: " + path + "; flags: " + flags);
		Node n = resolvePath(path);
		if (n instanceof FileNode) {
			FileNode s = (FileNode) n;
			s.open(flags);
			arg2.setFh(s);
			return 0;
		}
		return Errno.ENOENT;
	}

	@Override
	public int read(String path, Object fh, ByteBuffer buf, long offset) throws FuseException {
		log.debug("read: " + path + " " + offset);
		Node n = resolvePath(path);
		if (n instanceof FileNode) {
			FileNode s = (FileNode) n;
			s.read(buf, offset);
		}
		return 0;
	}

	public int write(String path, Object fh, boolean isWritepage, ByteBuffer buf, long offset) throws FuseException {
		log.debug("write: " + path + " " + offset);
		throw new FuseException("Read Only").initErrno(FuseException.EACCES);

		// if (outputStream == null) {
		// throw new FuseException("File Not Found")
		// .initErrno(FuseException.EACCES);
		// }
		// try {
		//
		// int i = 0;
		// while(buf.position()<buf.limit()) {
		// outputStream.write((int)buf.get());
		// }
		//
		// } catch (FileNotFoundException e) {
		// e.printStackTrace();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		//
	}

	public int flush(String path, Object fh) throws FuseException {
		log.debug("flush: " + path);
		if (fh instanceof FileNode)
			return 0;
		return Errno.EBADF;
	}

	private Node resolvePath(String path) throws FuseException {
		// if (SnmpFS.ROOTDIR.equals(path)) {
		// return tree.getRoot();
		// }
		// String oid = toOIDString(path);
		return tree.getNode(path);

		// if (name.equals(".")) {
		// // same node
		// } else if (name.equals("..")) {
		// // parent node
		// Node parentNode = node.getParent();
		// if (parentNode != null)
		// node = parentNode;
		// } else {
		// // child node
		// Node childNode = null;
		// if (node instanceof DirectoryNode && (childNode = ((DirectoryNode)
		// node).getChild(name)) != null)
		// node = childNode;
		// else
		// throw new FuseException("No such
		// node").initErrno(FuseException.ENOENT);
		// }
		//
		// subPathStart = i;
		// }
		//
	}

	public static void main(String[] args) {
		String fuseArgs[] = new String[args.length];
		System.arraycopy(args, 0, fuseArgs, 0, fuseArgs.length);
		log.info("mounting snmpfs");

		try {
			MibOps mibOps = new MibOps();
			FuseMount.mount(fuseArgs, new AgentFS(mibOps), log);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			log.info("exiting");
		}
	}


}
