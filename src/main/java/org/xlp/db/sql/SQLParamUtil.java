package org.xlp.db.sql;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.xlp.assertion.AssertUtils;
import org.xlp.db.sql.item.ComplexQueryFieldItem;
import org.xlp.db.sql.item.ComplexQueryFieldItem.ValueType;
import org.xlp.db.sql.limit.Limit;
import org.xlp.utils.collection.XLPCollectionUtil;

/**
 * <p>创建时间：2022年5月18日 下午12:15:33</p>
 * @author xlp
 * @version 1.0 
 * @Description SQL预处理参数获取工具类，针对于复杂查询
*/
class SQLParamUtil {
	/**
	 * 获取SQL预处理参数
	 * 
	 * @param complexQuerySQL
	 *  @param exeCount 标记是否是执行count语句
	 * @throws NullPointerException 假如参数为null，则抛出该异常
	 * @return
	 */
	public static Object[] getSqlParams(ComplexQuerySQL complexQuerySQL, boolean exeCount){
		AssertUtils.isNotNull(complexQuerySQL, "complexQuerySQL parameter is null!");
		List<ComplexQueryFieldItem> items = new LinkedList<ComplexQueryFieldItem>();
		complexQuerySQL = complexQuerySQL.getTopComplexQuerySQL();
		deepParseComplexQuerySQL(complexQuerySQL, items);
		items.addAll(complexQuerySQL.getHavingFieldItems());
		
		List<Object> params = itemToParam(items);
		Limit limit = complexQuerySQL.getLimit();
		if (limit != null && !exeCount) {
			params.add(limit.getStartPos());
			params.add(limit.getResultCount());
		}		
		return params.toArray();
	}
	
	/**
	 * 从条件中获取参数值
	 * 
	 * @param items
	 * @return
	 */
	private static List<Object> itemToParam(List<ComplexQueryFieldItem> items) {
		List<Object> params = new LinkedList<Object>();
		for (ComplexQueryFieldItem item : items) {
			switch (item.getOperator()) {
			case EQ: 
			case NEQ:
			case LIKE:
			case NLIKE:
			case GT:
			case GE:
			case LT:
			case LE:
				if (item.getValueType() == ValueType.VALUE) {
					params.add(item.getValue());
				}
				break;

			case IN:
			case NIN:
			case BETWEEN:
			case NBETWEEN:
				params.addAll(Arrays.asList(item.getValues())); 
				break;
				
			case NOT_EXISTS:
			case EXISTS:
				if (item.getValue() == ValueType.SQL) {
					params.addAll(Arrays.asList(((SQL)item.getValue()).getParams())); 
				}
				break;
			default:
				break;
			}
		}
		return params;
	}

	/**
	 * 递归解析SQL对象
	 * 
	 * @param complexQuerySQL
	 * @param items
	 */
	private static void deepParseComplexQuerySQL(ComplexQuerySQL complexQuerySQL, List<ComplexQueryFieldItem> items) {
		items.addAll(complexQuerySQL.getFieldItems());
		List<ComplexQuerySQL> childrenComplexQuerySQL = complexQuerySQL.getChildrenComplexQuerySQL();
		if (!XLPCollectionUtil.isEmpty(childrenComplexQuerySQL)) {
			for (ComplexQuerySQL child : childrenComplexQuerySQL) {
				deepParseComplexQuerySQL(child, items);
			}
		}
	}
}
