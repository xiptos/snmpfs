package pt.ipb.snmpfs;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.fusejna.DirectoryFiller;
import net.fusejna.ErrorCodes;
import net.fusejna.StructStat.StatWrapper;
import net.fusejna.types.TypeMode.NodeType;

import org.snmp4j.smi.OID;

public class SnmpFs extends AbstractFS {
	public static final String ROOTDIR = "/";

	FsTree tree;

	public SnmpFs(FsTree tree) {
		this.tree = tree;
	}

	@Override
	public int getattr(String path, StatWrapper stat) {
		FsEntry entry = resolvePath(path);
		if (entry != null) {
			NodeType t = null;
			switch (entry.getType()) {
			case BLOCK_DEVICE:
				t = NodeType.BLOCK_DEVICE;
				break;
			case FIFO:
				t = NodeType.FIFO;
				break;
			case SOCKET:
				t = NodeType.SOCKET;
				break;
			case DIRECTORY:
				t = NodeType.DIRECTORY;
				break;
			case FILE:
				t = NodeType.FILE;
				break;
			case SYMBOLIC_LINK:
				t = NodeType.SYMBOLIC_LINK;
				break;
			}
			stat.setMode(t, entry.isUr(), entry.isUw(), entry.isUx(), entry.isGr(), entry.isGw(), entry.isGx(),
					entry.isOr(), entry.isOw(), entry.isOx());
			return 0;
		}
		return ErrorCodes.ENOENT;
	};

	@Override
	public int readdir(String path, DirectoryFiller filler) {
		FsEntry node = resolvePath(path);
		for(int i=0; i<tree.getChildCount(node); i++) {
			FsEntry c = tree.getChild(node, i);
			filler.add(c.getName());
		}
		return 0;
	}

	protected String toOIDString(String path) {
		Pattern pattern = Pattern.compile("/");
		Matcher matcher = pattern.matcher(path);
		return matcher.replaceAll(".");
	}

	protected String oidToPath(OID oid) {
		StringBuilder str = new StringBuilder();
		for (int i : oid.getValue()) {
			str.append(ROOTDIR);
			str.append(i);
		}
		return str.toString();
	}

	/*
	 * @Override public int open(String path, int flags, FuseOpenSetter arg2)
	 * throws FuseException { log.debug("open: " + path + "; flags: " + flags);
	 * FsEntry n = resolvePath(path); if (n instanceof FileNode) { FileNode s =
	 * (FileNode) n; s.open(flags); arg2.setFh(s); return 0; } return
	 * Errno.ENOENT; }
	 * 
	 * @Override public int read(String path, Object fh, ByteBuffer buf, long
	 * offset) throws FuseException { log.debug("read: " + path + " " + offset);
	 * FsEntry n = resolvePath(path); if (n instanceof FileNode) { FileNode s =
	 * (FileNode) n; s.read(buf, offset); } return 0; }
	 * 
	 * public int write(String path, Object fh, boolean isWritepage, ByteBuffer
	 * buf, long offset) throws FuseException { log.debug("write: " + path + " "
	 * + offset); throw new
	 * FuseException("Read Only").initErrno(FuseException.EACCES);
	 * 
	 * // if (outputStream == null) { // throw new
	 * FuseException("File Not Found") // .initErrno(FuseException.EACCES); // }
	 * // try { // // int i = 0; // while(buf.position()<buf.limit()) { //
	 * outputStream.write((int)buf.get()); // } // // } catch
	 * (FileNotFoundException e) { // e.printStackTrace(); // } catch
	 * (IOException e) { // e.printStackTrace(); // } // }
	 * 
	 * public int flush(String path, Object fh) throws FuseException {
	 * log.debug("flush: " + path); if (fh instanceof FileNode) return 0; return
	 * Errno.EBADF; }
	 */
	private FsEntry resolvePath(String path) {
		String[] pth = path.split(File.pathSeparator);
		FsEntry node = tree.getRoot();
		for(String name : pth) {
			FsEntry child = tree.getChild(node, name);
			if(child!=null) {
				node = child;
			}
		}
		return node;
	}

	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Usage: SnmpFS <mountpoint>");
			System.exit(1);
		}
		try {
			SnmpFsTree tree = new SnmpFsTree("bart", "bartsimpson");
			tree.open();

			new SnmpFs(tree).log(true).mount(args[0]);
		} catch (final Throwable e) {
			System.err.println(e);
		}
	}

}
