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
 * �����ȡ�������ݿ����б��ṹ����ṹ�Ĺ�ϵ�������Ը��ݱ��ṹ������ṹ��
 * ֱ��ȥ������cv
 * @author gaoqi www.sxt.cn
 *
 */
public class TableContext {
  
/**
 * ����Ϊkey������Ϣ����Ϊvalue
 */
public static  Map<String,TableInfo>  
tables = new HashMap<String,TableInfo>();
/**
 * ��po��class����ͱ���Ϣ��������������������ã�
 */
public static  Map<Class,TableInfo>  
poClassTableMap = new HashMap<Class,TableInfo>();
private TableContext(){
 
}
static {
	try {
	//��ʼ����ñ�����Ϣ
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
	//��ѯ���е������ֶ�
	while(set.next()){
	ColumnInfo ci = new ColumnInfo(set.getString("COLUMN_NAME"), 
	set.getString("TYPE_NAME"), 0);
	ti.getColumns().put(set.getString("COLUMN_NAME"), ci);
	}
	 
	ResultSet set2 = dbmd.getPrimaryKeys(null, "%", tableName);  
	//��ѯt_user���е�����
	while(set2.next()){
	ColumnInfo ci2 = (ColumnInfo) 
	ti.getColumns().get(set2.getObject("COLUMN_NAME"));
	ci2.setKeyType(1);  
	//����Ϊ��������
	ti.getPriKeys().add(ci2);
	}
	 
	if(ti.getPriKeys().size()>0)
	{  //ȡΨһ������������ʹ�á������������������Ϊ�գ�
	ti.setOnlyPriKey(ti.getPriKeys().get(0));
	}
	}
	} catch (SQLException e) {
	e.printStackTrace();
	}  
	
	//ÿ���������������µ�����
	updateJavaPOFile();
	
	//����po���µ����е��� ���ڸ���
	loadPOTables();
}

/**
 * ���ݱ��ṹ ����po���µ�java��
 * ʵ���˱��ṹ����ṹ �����Ի���µ�������ȥ
 */
public static void updateJavaPOFile() {
	Map<String, TableInfo> map = TableContext.tables;
	
	for(TableInfo ti : map.values()) {
		JavaFileUtils.createJavaPOFile(ti, new MysqlTypeConvertor());
	}
}

/**
 * ����po���µ��� 
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