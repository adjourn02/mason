package sim.field.grid;

import java.io.IOException;

import mpi.*;

import sim.util.IntPoint;
import sim.util.IntHyperRect;
import sim.util.MPITest;
import sim.field.DPartition;
import sim.field.DNonUniformPartition;
import sim.field.HaloField;
import sim.field.storage.DoubleGridStorage;

public class NDoubleGrid2D extends HaloField {

	public NDoubleGrid2D(DPartition ps, int[] aoi, double initVal) {
		super(ps, aoi, new DoubleGridStorage(ps.getPartition(), initVal));

		if (this.nd != 2)
			throw new IllegalArgumentException("The number of dimensions is expected to be 2, got: " + this.nd);
	}

	public double[] getStorageArray() {
		return (double[])field.getStorage();
	}

	public final double get(final int x, final int y) {
		return get(new IntPoint(x, y));
	}

	public final double get(IntPoint p) {
		// In this partition and its surrounding ghost cells
		if (!inLocalAndHalo(p))
			throw new IllegalArgumentException(String.format("PID %d get %s is out of local boundary", ps.getPid(), p.toString()));

		return getStorageArray()[field.getFlatIdx(toLocalPoint(p))];
	}

	public final void set(final int x, final int y, final double val) {
		set(new IntPoint(x, y), val);
	}

	public final void set(IntPoint p, final double val) {
		// In this partition but not in ghost cells
		if (!inLocal(p))
			throw new IllegalArgumentException(String.format("PID %d set %s is out of local boundary", ps.getPid(), p.toString()));

		getStorageArray()[field.getFlatIdx(toLocalPoint(p))] = val;
	}

	public final int stx(final int x) {
		return toToroidal(x, 0);
	}

	public final int sty(final int y) {
		return toToroidal(y, 1);
	}

	public static void main(String[] args) throws MPIException, IOException {
		MPI.Init(args);

		int[] aoi = new int[] {2, 2};
		int[] size = new int[] {8, 8};

		DNonUniformPartition p = new DNonUniformPartition(size, true);
		p.initUniformly(null);
		p.setMPITopo();

		NDoubleGrid2D f = new NDoubleGrid2D(p, aoi, p.getPid());

		f.sync();

		MPITest.execInOrder(i -> System.out.println(f), 500);

		MPI.Finalize();
	}
}
