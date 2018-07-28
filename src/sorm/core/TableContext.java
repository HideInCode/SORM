package sorm.core;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
  
import sorm.bean.ColumnInfo;
import sorm.bean.TableInfo;
import sorm.util.JavaFileUtils;
import sorm.util.StringUtils;
  
/**
 * 负责获取管理数据库所有表结构和类结构的关系，并可以根据表结构生成类结构。
 * 直接去官网上cv
 * @author gaoqi www.sxt.cn
 *
 */
public class TableContext {
  
/**
 * 表名为key，表信息对象为value
 */
public static  Map<String,TableInfo>  
tables = new HashMap<String,TableInfo>();
/**
 * 将po的class对象和表信息对象关联起来，便于重用！
 */
public static  Map<Class,TableInfo>  
poClassTableMap = new HashMap<Class,TableInfo>();
private TableContext(){
 
}
static {
	try {
	//初始化获得表的信息
	Connection con = DBManager.getConn();
	DatabaseMetaData dbmd = con.getMetaData(); 
	 
	ResultSet tableRet = dbmd.getTables(null,
	 "%","%",new String[]{"TABLE"}); 
	 
	while(tableRet.next()){
	String tableName = (String) 
	tableRet.getObject("TABLE_NAME");
	 
	TableInfo ti = new TableInfo(tableName, 
	new ArrayList<ColumnInfo>()
	,new HashMap<String, ColumnInfo>());
	tables.put(tableName, ti);
	 
	ResultSet set = dbmd.getColumns(null, "%", tableName, "%");  
	//查询表中的所有字段
	while(set.next()){
	ColumnInfo ci = new ColumnInfo(set.getString("COLUMN_NAME"), 
	set.getString("TYPE_NAME"), 0);
	ti.getColumns().put(set.getString("COLUMN_NAME"), ci);
	}
	 
	ResultSet set2 = dbmd.getPrimaryKeys(null, "%", tableName);  
	//查询t_user表中的主键
	while(set2.next()){
	ColumnInfo ci2 = (ColumnInfo) 
	ti.getColumns().get(set2.getObject("COLUMN_NAME"));
	ci2.setKeyType(1);  
	//设置为主键类型
	ti.getPriKeys().add(ci2);
	}
	 
	if(ti.getPriKeys().size()>0)
	{  //取唯一主键。。方便使用。如果是联合主键。则为空！
	ti.setOnlyPriKey(ti.getPriKeys().get(0));
	}
	}
	} catch (SQLException e) {
	e.printStackTrace();
	}  
	
	//每次启动都包含最新的属性
	updateJavaPOFile();
	
	//加载po包下的所有的类 便于复用
	loadPOTables();
}

/**
 * 根据表结构 更新po包下的java类
 * 实现了表结构到类结构 新属性会更新到类里面去
 */
public static void updateJavaPOFile() {
	Map<String, TableInfo> map = TableContext.tables;
	
	for(TableInfo ti : map.values()) {
		JavaFileUtils.createJavaPOFile(ti, new MysqlTypeConvertor());
	}
}

/**
 * 加载po包下的类 
 */
public static void loadPOTables() {
	
	for(TableInfo ti : tables.values()) {
		
		try {
			Class c = Class.forName(DBManager.getConf().getPoPackage()+"."+StringUtils.firstChar2UpperCase(ti.getName()));
			poClassTableMap.put(c, ti);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
}

public static void main(String[] args) {
 Map<String,TableInfo>  tables = TableContext.tables;
 System.out.println(tables);
}
  
}