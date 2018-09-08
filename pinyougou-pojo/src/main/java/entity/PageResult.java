package entity;

import java.util.List;
import java.io.Serializable;

import com.pinyougou.pojo.TbBrand;

/**
 * 分页实体类
 * @author liq
 *
 */

public class PageResult implements Serializable {

	private long total; //总记录数
	private List rows;	//当前页记录
	public PageResult(long total, List<?> rows) {
		super();
		this.total = total;
		this.rows = rows;
	}
	public long getTotal() {
		return total;
	}
	public void setTotal(long total) {
		this.total = total;
	}
	public List getRows() {
		return rows;
	}
	public void setRows(List rows) {
		this.rows = rows;
	}
	
	
}
