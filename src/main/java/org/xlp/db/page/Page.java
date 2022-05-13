package org.xlp.db.page;

import java.util.ArrayList;
import java.util.List;

import org.xlp.db.utils.Constants;
import org.xlp.javabean.annotation.Bean;
import org.xlp.javabean.annotation.FieldName;
import org.xlp.utils.collection.XLPCollectionUtil;

/**
 * 分页信息对象
 * 
 * @author 徐龙平
 * @version 1.0
 * 
 */
@Bean
public class Page<T> {
	//每页的数据信息集合
	@FieldName
	private List<T> datas;
	//当前页面号
	@FieldName
	private int currentNo = 1;
	//每页数据现显示的条数
	@FieldName
	private int pageSize = Constants.DEFAULT_PAGE_SIZE;
	//数据总条数
	@FieldName
	private long totalCount = 0;
	//当前页面实际显示的数据条数
	@FieldName
	private int currentCount = 0;
	//总页数
	@FieldName
	private int totalPage = 1;
	//页码显示数量
	@FieldName
	private int pageCodeNo = Constants.DEFAULT_PAGE_CODE_NUM;
	//是否有前一页
	@FieldName
	private boolean hasPrev;	
	//是否有下一页
	@FieldName
	private boolean hasNext;
	//翻页行可以出现的连续的页码集合默认5个
	@FieldName
	private List<Integer> clickPageNo;
	
	public Page(){
	}
	
	/**
	 * @param currentNo 当前页面号，从1开始
	 * @param pageSize 每页显示数据的大小
	 */
	public Page(int currentNo, int pageSize){
		setCurrentNo(currentNo);
		setPageSize(pageSize);
	}
	
	/**
	 * @param currentNo 当前页面号，从1开始
	 * @param pageSize 每页显示数据的大小
	 * @param pageCodeNo 页码显示数量
	 */
	public Page(int currentNo, int pageSize, int pageCodeNo) {
		this(currentNo, pageSize);
		setPageCodeNo(pageCodeNo);
	}
	
	public Page(List<T> datas, int currentNo, long totalCount) {
		this.datas = datas;
		setCurrentNo(currentNo);
		setTotalCount(totalCount);
	}

	/**
	 * @param datas
	 * @param currentNo 当前页面号，从1开始
	 * @param pageSize
	 * @param totalCount
	 * @param pageCodeNo
	 */
	public Page(List<T> datas, int currentNo, int pageSize, long totalCount,
			int pageCodeNo) {
		this.datas = datas;
		setCurrentNo(currentNo);
		setPageSize(pageSize);
		setTotalCount(totalCount);
		setPageCodeNo(pageCodeNo);
	}

	public List<T> getDatas() {
		return datas;
	}
	
	public void setDatas(List<T> datas) {
		this.datas = datas;
	}
	
	public int getCurrentNo() {
		return currentNo;
	}
	
	public void setCurrentNo(int currentNo) {
		this.currentNo = currentNo < 1 ? 1 : currentNo;
	}
	
	public int getPageSize() {
		return pageSize;
	}
	
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize < 0 ? 0 : pageSize ;
	}
	
	public long getTotalCount() {
		return totalCount;
	}
	
	public void setTotalCount(long totalCount) {
		this.totalCount = totalCount < 0 ? 0 : totalCount ;
	}
	
	public int getPageCodeNo() {
		return pageCodeNo;
	}
	
	public void setPageCodeNo(int pageCodeNo) {
		this.pageCodeNo = pageCodeNo < 1 ? 5 : pageCodeNo;
	}
	
	public int getTotalPage() {
		if(pageSize == 0 || totalCount / pageSize == 0)
			return totalPage;
		totalPage = (int) (totalCount % pageSize != 0 ? totalCount / pageSize + 1
				: totalCount / pageSize);
		return totalPage;
	}
	
	public boolean isHasPrev() {
		hasPrev = currentNo != 1;
		return hasPrev;
	}
	
	public boolean isHasNext() {
		hasNext = currentNo != getTotalPage();
		return hasNext;
	}
	
	public int getCurrentCount() {
		currentCount = datas == null ? 0 : datas.size();
		return currentCount;
	}
	
	public List<Integer> getClickPageNo() {
		if(XLPCollectionUtil.isEmpty(datas))
			return new ArrayList<Integer>(0);
		totalPage = getTotalPage();
		if(totalPage <= pageCodeNo){
			clickPageNo = new ArrayList<Integer>(totalPage);
			initClickPageNo(1, totalPage);
		}else {
			clickPageNo = new ArrayList<Integer>(pageCodeNo);
			int startNo = currentNo - pageCodeNo / 2;//开始页码
			int endNo = startNo + pageCodeNo -1; //结尾页码
			if(startNo <= 1)
				initClickPageNo(1, pageCodeNo);
			else if(endNo >= totalPage )
				initClickPageNo(totalPage - pageCodeNo + 1, pageCodeNo);
			else
				initClickPageNo(startNo, pageCodeNo);
		}
		
		return clickPageNo;
	}

	private void initClickPageNo(int startIndex, int count) {
		for (int i = startIndex; i <= count; i++) {
			clickPageNo.add(Integer.valueOf(i));
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Page [clickPageNo=");
		builder.append(clickPageNo);
		builder.append(", currentCount=");
		builder.append(currentCount);
		builder.append(", currentNo=");
		builder.append(currentNo);
		builder.append(", datas=");
		builder.append(datas);
		builder.append(", hasNext=");
		builder.append(hasNext);
		builder.append(", hasPrev=");
		builder.append(hasPrev);
		builder.append(", pageCodeNo=");
		builder.append(pageCodeNo);
		builder.append(", pageSize=");
		builder.append(pageSize);
		builder.append(", totalCount=");
		builder.append(totalCount);
		builder.append(", totalPage=");
		builder.append(totalPage);
		builder.append("]");
		return builder.toString();
	}
}
