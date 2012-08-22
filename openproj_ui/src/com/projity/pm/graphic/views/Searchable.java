package com.projity.pm.graphic.views;

import java.util.List;

public interface Searchable {
	boolean findNext(SearchContext context);
	SearchContext createSearchContext();
	List getAvailableFields();
}
