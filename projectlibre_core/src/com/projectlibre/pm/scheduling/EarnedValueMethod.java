package com.projectlibre.pm.scheduling;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Laurent Chretienneau
 *
 */
public enum EarnedValueMethod {
	//same constants as mpxj to simplify
	   PERCENT_COMPLETE(0),
	   PHYSICAL_PERCENT_COMPLETE(1);

	protected int id;
	protected static Map<Integer,EarnedValueMethod> reverseMap;

	private EarnedValueMethod(int id){
		this.id=id;
	}
	public int getId() {
		return id;
	}
	public static EarnedValueMethod getInstance(int id){
		if (reverseMap==null){
			reverseMap=new HashMap<Integer,EarnedValueMethod>();
			for (EarnedValueMethod ct : values())
				reverseMap.put(ct.getId(),ct);
		}
		return reverseMap.get(id);
	}

}
