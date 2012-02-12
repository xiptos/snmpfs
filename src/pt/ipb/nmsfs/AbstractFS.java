package pt.ipb.nmsfs;

import java.io.File;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import fuse.Errno;
import fuse.Filesystem3;
import fuse.FuseException;
import fuse.FuseSizeSetter;
import fuse.FuseStatfsSetter;
import fuse.XattrLister;
import fuse.XattrSupport;

public abstract class AbstractFS implements Filesystem3, XattrSupport {
	public static final int BLOCK_SIZE = 512;

	public static final int NAME_LENGTH = 1024;

	public static final String PATHSEP = File.separator;

	public int statfs(FuseStatfsSetter statfsSetter) throws FuseException {
		statfsSetter.set(BLOCK_SIZE, 1000, 200, 180, 0, 0, NAME_LENGTH);

		return 0;
	}

	public static String getLastPathComponent(String path) {
		String[] pathCmpnts = path.split(PATHSEP);
		return pathCmpnts[pathCmpnts.length-1];
	}

	public static String getPathComponent(String path, int i) {
		if (path.startsWith(PATHSEP))
			i++;
		String[] pathCmpnts = path.split(PATHSEP);
		return pathCmpnts[i];
	}

	public static String removePathComponent(String path, int i) {
		if (path.startsWith(PATHSEP))
			i++;
		String[] pathCmpnts = path.split(PATHSEP);
		StringBuilder str = new StringBuilder(PATHSEP);
		for (int t = 0; t < pathCmpnts.length; t++) {
			if (t != i && pathCmpnts[t] != null && pathCmpnts[t].length() > 0) {
				if (!str.toString().endsWith(PATHSEP)) {
					str.append(PATHSEP);
				}
				str.append(pathCmpnts[t]);
			}
		}
		return str.toString();
	}

	public int readlink(String path, CharBuffer link) throws FuseException {
		throw new FuseException().initErrno(Errno.ENOLINK);
	}

	public int mkdir(String path, int mode) throws FuseException {
		return Errno.EROFS;
	}

	public int mknod(String path, int mode, int rdev) throws FuseException {
		return Errno.EROFS;
	}

	public int symlink(String from, String to) throws FuseException {
		return Errno.EROFS;
	}

	public int truncate(String path, long size) throws FuseException {
		return Errno.EROFS;
	}

	public int unlink(String path) throws FuseException {
		return Errno.EROFS;
	}

	public int utime(String path, int atime, int mtime) throws FuseException {
		return 0;
	}

	public int chmod(String path, int mode) throws FuseException {
		return 0;
	}

	public int chown(String path, int uid, int gid) throws FuseException {
		return 0;
	}

	public int link(String from, String to) throws FuseException {
		return Errno.EROFS;
	}

	public int rename(String from, String to) throws FuseException {
		return Errno.EROFS;
	}

	public int rmdir(String path) throws FuseException {
		return Errno.EROFS;
	}

	// (called when last filehandle is closed), fh is filehandle passed from
	// open
	public int release(String path, Object fh, int flags) throws FuseException {
//		if (fh instanceof FH) {
//			FH f = (FH) fh;
//			loadMibsFromCacheDir();
//			f.release();
//			System.runFinalization();
//			return 0;
//		}
//
		return 0;
	}

	// new operation (Synchronize file contents), fh is filehandle passed from
	// open,
	// isDatasync indicates that only the user data should be flushed, not the
	// meta data
	public int fsync(String path, Object fh, boolean isDatasync) throws FuseException {
		return Errno.EBADF;
	}

	@Override
	public int getxattr(String path, String name, ByteBuffer dst) throws FuseException, BufferOverflowException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getxattrsize(String path, String name, FuseSizeSetter sizeSetter) throws FuseException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int listxattr(String path, XattrLister lister) throws FuseException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int removexattr(String path, String name) throws FuseException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int setxattr(String path, String name, ByteBuffer value, int flags) throws FuseException {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
	
}
