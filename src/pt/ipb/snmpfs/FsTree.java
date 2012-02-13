/**
 * $Id$
@copyright@
@license@
 */

package pt.ipb.snmpfs;



public interface FsTree {

	FsEntry getChild(FsEntry parent, int i);

	int getChildCount(FsEntry parent);

	FsEntry getRoot();

	boolean isLeaf(FsEntry n);

	FsEntry getChild(FsEntry parent, String name);

}
