package org.xlp;
import java.sql.Time;
import java.sql.Timestamp;

import org.xlp.db.tableoption.annotation.XLPColumn;
import org.xlp.db.tableoption.annotation.XLPEntity;
import org.xlp.db.tableoption.annotation.XLPId;
import org.xlp.db.tableoption.xlpenum.PrimaryKeyType;

@XLPEntity(tableName = "account")
public class Account {
	@XLPColumn( columnName = "name")
	private String name;
	@XLPId(columnName = "id")
	private int id;
	@XLPColumn(columnName = "money")
	private double money;
	private Timestamp date;
	private java.sql.Date d;
	private Time time;
	@XLPId(columnName = "uuid",type=PrimaryKeyType.UUID)
	private String uuid;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public double getMoney() {
		return money;
	}
	public void setMoney(double money) {
		this.money = money;
	}
	public Timestamp getDate() {
		return date;
	}
	public void setDate(Timestamp date) {
		this.date =  date;
	}
	public java.sql.Date getD() {
		return d;
	}
	public void setD(java.sql.Date d) {
		this.d = d;
	}
	public Time getTime() {
		return time;
	}
	public void setTime(Time time) {
		this.time = time;
	}
	
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Account [name=").append(name).append(", id=")
				.append(id).append(", money=").append(money).append(", date=")
				.append(date).append(", d=").append(d).append(", time=")
				.append(time).append(", uuid=").append(uuid).append("]");
		return builder.toString();
	}

	
}
