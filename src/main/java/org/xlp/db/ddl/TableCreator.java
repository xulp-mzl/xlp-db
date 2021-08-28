package org.xlp.db.ddl;

/**
 * <p>创建时间：2021年8月29日 上午12:08:14</p>
 * @author xlp
 * @version 1.0 
 * @Description table创建接口
*/
public interface TableCreator {
	/**
	 * 创建表
	 */
	public void createTables();
	
	/**
	 * 通过实体对象创建数据库表
	 * 
	 * @param entity
	 *            实体对象
	 */
	public <T> void createTableByEntity(T entity);
	
	/**
	 * 通过实体类创建数据库表
	 * 
	 * @param entityClass
	 *            实体类
	 */
	public <T> void createTableByEntityClass(Class<T> entityClass);
}
