package alpv.mwp;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class TaskImpl implements Task<List<Integer>,List<Integer>> {

	@Override
	public List<Integer> exec(List<Integer> a) {
			Collections.sort(a);
			return a;
	}

}
