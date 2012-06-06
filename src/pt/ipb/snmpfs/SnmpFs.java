package pt.ipb.snmpfs;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import net.fusejna.DirectoryFiller;
import net.fusejna.ErrorCodes;
import net.fusejna.StructFuseFileInfo.FileInfoWrapper;
import net.fusejna.StructStat.StatWrapper;
import net.fusejna.types.TypeMode.NodeType;
import pt.ipb.marser.MibException;
import pt.ipb.snmpfs.prefs.Device;
import pt.ipb.snmpfs.prefs.Entry;
import pt.ipb.snmpfs.prefs.Table;
import pt.ipb.snmpfs.prefs.XmlHandler;

public class SnmpFs extends AbstractFS {

	Device device;
	List<FsEntry> entries = new ArrayList<FsEntry>();
	private SnmpBackend backend;
	MibBackend mibBackend;

	public SnmpFs(Device device) throws IOException, MibException {
		this.device = device;
		this.backend = new SnmpBackend(device.getSnmpPrefs());
		this.mibBackend = new MibBackend(device.getMibDir(), device.getMibs());
		
		initEntries();
	}

	private void initEntries() {
		entries.add(new WalkEntry(backend, mibBackend));
//		entries.add(new HelloEntry());
		for(Entry entry : device.getEntries()) {
			if(entry instanceof Table) {
				entries.add(new TableEntry((Table)entry, backend, mibBackend));
			} else {
				entries.add(new ScalarEntry(entry, backend, mibBackend));				
			}
		}
	}

	@Override
	public int getattr(String path, StatWrapper stat) {
		if (path.equals(PATHSEP)) { // Root directory
			stat.setMode(NodeType.DIRECTORY);
			return 0;
		}

		FsEntry entry = resolveEntry(path);

		NodeType t = null;
		if (entry != null) {
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
					entry.isOr(), entry.isOw(), entry.isOx()).size(entry.size());
			return 0;
		}
		return -ErrorCodes.ENOENT;
	};

	private FsEntry resolveEntry(String path) {
		String name = path.substring(1);
		for (FsEntry entry : entries) {
			if (name.equals(entry.getName())) {
				return entry;
			}
		}
		return null;
	}

	@Override
	public int readdir(String path, DirectoryFiller filler) {
		for (FsEntry entry : entries) {
			filler.add(PATHSEP+entry.getName());
		}
		return 0;
	}

	// protected String toOIDString(String path) {
	// Pattern pattern = Pattern.compile("/");
	// Matcher matcher = pattern.matcher(path);
	// return matcher.replaceAll(".");
	// }
	//
	// protected String oidToPath(OID oid) {
	// StringBuilder str = new StringBuilder();
	// for (int i : oid.getValue()) {
	// str.append(ROOTDIR);
	// str.append(i);
	// }
	// return str.toString();
	// }

	@Override
	public int read(String path, ByteBuffer buffer, long size, long offset, FileInfoWrapper info) {
		// Compute substring that we are being asked to read
		FsEntry entry = resolveEntry(path);
		if (entry != null) {
			return entry.read(path, buffer, size, offset);
		}
		return ErrorCodes.ENOENT;
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
	// private FsEntry resolvePath(String path) {
	// String[] pth = path.split(File.pathSeparator);
	// FsEntry node = device.getRoot();
	// for (String name : pth) {
	// FsEntry child = device.getChild(node, name);
	// if (child != null) {
	// node = child;
	// }
	// }
	// return node;
	// }

	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Usage: SnmpFS <conf_file>");
			System.exit(1);
		}
		try {
			Device device = XmlHandler.parse(new FileInputStream(args[0]));
			SnmpFs fs = new SnmpFs(device);
			fs.log(false).mount(device.getMountDir());
		} catch (final Throwable e) {
			e.printStackTrace();
		}
	}

}
