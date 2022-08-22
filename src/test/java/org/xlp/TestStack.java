package org.xlp;

import java.sql.Date;
import java.sql.Time;
import java.util.Arrays;
import java.util.Stack;

import org.xlp.db.sql.ComplexQuerySQL;
import org.xlp.db.sql.UnionSQL;
import org.xlp.db.sql.limit.Limit;
import org.xlp.utils.XLPDateUtil;

/**
 * <p>创建时间：2022年5月14日 下午3:25:00</p>
 * @author xlp
 * @version 1.0 
 * @Description 类描述
*/
public class TestStack {
	public static void main(String[] args) {
		Stack<String> stack = new Stack<>();
		stack.push("12");
		stack.push("23");
		stack.push("34");
		System.out.println(stack);
		while (!stack.isEmpty()) {
			System.out.println(stack.pop());
		}
		System.out.println(stack);
		Object[] values = new Object[]{1,1};
		values = Arrays.copyOf(values, 4);
		values[2] = "23";
		System.out.println(Arrays.toString(values));
		System.out.println(" h\r\nr h k ".replaceAll("\\s", "'")
				.replace('\'', '"').replace('"', '@'));
		ComplexQuerySQL sql = ComplexQuerySQL.of(Account.class, "a");
		System.out.println(sql.getParamSql());
		sql.innerJoin(Account.class, "b").andEq("a.name", "b.id", true).group().andEq("a.name", "3")
			.orEq("id", "4").endGroup()/*.limit(new Limit(1, 4))*/;
		sql.andEq("name", "t").property("a.name", "kj");
		System.out.println(sql.getParamSql());
		System.out.println(sql.getSql());
		System.out.println(sql.countSql().getParamSql());
		UnionSQL unionSQL = UnionSQL.of(sql);
		unionSQL.union(sql).limit(new Limit(0, 7));
		System.out.println("------");
		System.out.println(unionSQL.getParamSql());
		System.out.println(XLPDateUtil.isDate(String.class));
	}
}
