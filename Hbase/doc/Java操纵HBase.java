package com.qianfeng;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.filter.BinaryComparator;
import org.apache.hadoop.hbase.filter.BinaryPrefixComparator;
import org.apache.hadoop.hbase.filter.ByteArrayComparable;
import org.apache.hadoop.hbase.filter.ColumnPrefixFilter;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.FamilyFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.MultipleColumnPrefixFilter;
import org.apache.hadoop.hbase.filter.PrefixFilter;
import org.apache.hadoop.hbase.filter.QualifierFilter;
import org.apache.hadoop.hbase.filter.FilterList.Operator;
import org.apache.hadoop.hbase.filter.RegexStringComparator;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.filter.SubstringComparator;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Test;

public class TestHBash {

	
	/**
	 * 创建一个t_user表
	 * 步骤：
	 * 	1. 连接hbase服务器
	 *  2. 给表起个名字
	 *  3. 指定列族
	 * @throws Exception 
	 */
	@Test
	public void createTable() throws Exception{
		//创建一个configuration
		Configuration conf = HBaseConfiguration.create();
		/**
		 * 1.访问“单机版”的hbase服务的方式
		 * 		conf.set("hbase.zookeeper.quorum", "min1")
		 * 2.访问“伪分布式”的hbase服务的方式
		 * 		conf.set("hbase.zookeeper.quorum", "min2")
		 * 3.访问“全分布式”的hbase服务的方式
		 * 		conf.set("hbase.zookeeper.quorum", "ha5,ha6,ha7")
		 */
		//指定链接hbase服务器的url
		conf.set("hbase.zookeeper.quorum", "min1");//min1代表的是访问“单机版”的hbase服务
		
		//真正连接上hbase服务器
		Connection connection = ConnectionFactory.createConnection(conf);
		
		
		//同admin完成ddl工作  如创建表
		Admin admin = connection.getAdmin();
		//封装表信息的类    比如  表信息包括   表名
		TableName tn = TableName.valueOf("t_user");
		HTableDescriptor hd = new HTableDescriptor(tn);
		//创建一个列族
		HColumnDescriptor bi_family = new HColumnDescriptor("base_info");
		hd.addFamily(bi_family);
		
		HColumnDescriptor ei_family = new HColumnDescriptor("extra_info");
		hd.addFamily(ei_family);
		
		admin.createTable(hd);
		admin.close();
	}
	
	
	/**
	 * 往t_user表中添加一条记录
	 * 步骤：
	 *   1.连接hbase服务器
	 *   2.指定一个表
	 *   3.指定一个rowkey
	 *   4.往列族中添加key value对
	 *   
	 */
	@Test
	public void testPut() throws Exception{
		Configuration conf = HBaseConfiguration.create();
		conf.set("hbase.zookeeper.quorum", "min1");
//		Connection connection = ConnectionFactory.createConnection();
		
		//指定一个表   如t_user
		HTable table = new HTable(conf, "t_user");
		//指定一个rowkey      如 bj_0001
		Put put = new Put(Bytes.toBytes("sz_0004"));
		put.addColumn("base_info".getBytes(), "name".getBytes(), "wem".getBytes());
		put.addColumn("base_info".getBytes(), "age".getBytes(), "8".getBytes());
		
		put.addColumn("extra_info".getBytes(), "caceer".getBytes(), "fomer".getBytes());
		put.addColumn("extra_info".getBytes(), "hobby".getBytes(), "sleepping".getBytes());
		
		table.put(put);
		table.flushCommits();
		table.close();
	}
	
	/**
	 * 查询一条记录，将该条记录的信息显示出来
	 * 1.指定一个表
	 * 2.指定一个rowkey
	 * 
	 */
	@Test
	public void testGet() throws Exception{
		Configuration conf = HBaseConfiguration.create();
		conf.set("hbase.zookeeper.quorum", "min1");
		
		//指定要访问的表
		HTable table = new HTable(conf, "t_user");
		
		//指定要访问的rowkey
		Get get = new Get(Bytes.toBytes("bj_0001"));
		
		Result result = table.get(get);
		
		//获得当前的行的所有信息，包括列族信息、列的信息和列的值的信息
		java.util.List<KeyValue> kvs = result.list();
		for(KeyValue kv : kvs){
			String family = new String(kv.getFamily());//列族名  如  base_info
			System.out.println(family);
			
			String quelifier =  new String(kv.getQualifier());//列名 如  “name:zhangsan”中的name
			System.out.println("    "+quelifier);
			
			String value = new String(kv.getValue());//列的值  如  “name:zhangsan”中的zhansan
			System.out.println("    "+value);
		}
		table.close();
	}
	
	
	/**
	 * 获取一个行中的某个列族下的某列的的值
	 */
	@Test
	public void testGet2() throws Exception{
		Configuration conf = HBaseConfiguration.create();
		conf.set("hbase.zookeeper.quorum", "min1");
		HTable table = new HTable(conf, "t_user");
				
		Get get = new Get(Bytes.toBytes("bj_0001"));
		
		Result reslut = table.get(get);
		
		byte[] age_bytes = reslut.getValue(Bytes.toBytes("extra_info"), Bytes.toBytes("hobby"));
		System.out.println(new String(age_bytes));
				
	}
	
	//全表扫描   select * from t_user limit 0,5
	@Test
	public void testScanData() throws Exception{
		
		Configuration conf = HBaseConfiguration.create();
		conf.set("hbase.zookeeper.quorum", "min1");
		
		HTable table = new HTable(conf, "t_user");
		
		Scan scan = new Scan();//指定一个扫描器
		//下面两句类似于 limit 语句    ，   最终获得的值是  bj_0001开始（包括该行信息） ，到bj_0003这行的前一条记录（不包括该行记录）
		//最终结果为   bj_0001和bj_0002两条记录的数据
		scan.setStartRow(Bytes.toBytes("bj_0001"));
		scan.setStopRow(Bytes.toBytes("bj_0003"));
		ResultScanner rs = table.getScanner(scan);//开始扫描
		for(Result r : rs){//Result包含行（记录）的所有列族的信息   当然也包括列族下的所有列的信息。
			byte[] bs_name = r.getValue(Bytes.toBytes("base_info"), Bytes.toBytes("name"));
			String nameV = new String(bs_name);
			System.out.println(nameV);
		}
		
	}
	//全表扫描  不拿行（记录）的所有列族的信息  只是拿列族base_info下的信息
	//类似于    select base_info  from t_user;   
	@Test
	public void testScanData2() throws Exception{
		Configuration conf = HBaseConfiguration.create();
		conf.set("hbase.zookeeper.quorum", "min1");
		
		HTable table = new HTable(conf, "t_user");
		Scan scan = new Scan();//指定一个扫描器
		scan.addFamily(Bytes.toBytes("base_info"));//指定只去取base_info列族的信息
		
		ResultScanner rs = table.getScanner(scan);//开始扫描
		for(Result r : rs){//Result包含行（记录）的名为base_info列族的信息   其他的列族信息是没有
			byte[] bs_name = r.getValue(Bytes.toBytes("extra_info"), Bytes.toBytes("hobby"));
			if(bs_name!=null){
				String nameV = new String(bs_name);
				System.out.println(nameV);
			}
		}
	}
		//全表扫描  不拿行（记录）的所有列族的信息  只是拿base_info：name列的信息
		//类似于    select base_info:name  from t_user;   
		@Test
		public void testScanData3() throws Exception{
			Configuration conf = HBaseConfiguration.create();
			conf.set("hbase.zookeeper.quorum", "min1");
			
			HTable table = new HTable(conf, "t_user");
			Scan scan = new Scan();//指定一个扫描器
			scan.addColumn(Bytes.toBytes("base_info"), Bytes.toBytes("name"));//指定只去取base_info列族的name列的信息
			ResultScanner rs = table.getScanner(scan);//开始扫描
			for(Result r : rs){//Result包含行（记录）的名为base_info列族下的名name列的信息   其他的列的信息是没有
				
				byte[] bs_age = r.getValue(Bytes.toBytes("base_info"), Bytes.toBytes("age"));
				if(bs_age!=null){
					String nameV = new String(bs_age);
					System.out.println(nameV);
				}
			}
		}
		
	//全表扫描的过滤器     按”列值“过滤 
	//类似于    select * from t_user where extra_info.hobby = 'football'
	@Test
	public void testScanDateFilter() throws Exception{
		Configuration conf = HBaseConfiguration.create();
		conf.set("hbase.zookeeper.quorum", "min1");
		HTable table = new HTable(conf, "t_user");
		Scan scan = new Scan();
		
		//==================================================================================
		//”列的值“是过滤条件 
		SingleColumnValueFilter scvf = new SingleColumnValueFilter(
				Bytes.toBytes("extra_info"), Bytes.toBytes("hobby"), 
				CompareOp.EQUAL,
				Bytes.toBytes("football"));//这一条语句  类似于  （where extra_info.hobby = 'football'）这个条件
		scan.setFilter(scvf);
		//==================================================================================

		
		ResultScanner rss = table.getScanner(scan);
		
		for(Result rs : rss){
			byte[] bs_name = rs.getValue(Bytes.toBytes("base_info"),Bytes.toBytes("name"));
			if(bs_name!=null){
				System.out.println(Bytes.toString(bs_name));
			}
		}
	}
	
	//按rowkey过滤
	@Test
	public void scanDateFilterByRowkey() throws Exception{
		Configuration conf = HBaseConfiguration.create();
		conf.set("hbase.zookeeper.quorum", "min1");
		HTable table =new HTable(conf, "t_user");
		Scan scan = new Scan();
		
		//======================过滤出rowkey的值以bj开头所有记录==============================
		RowFilter rf = new RowFilter(CompareFilter.CompareOp.EQUAL,new RegexStringComparator("bj+"));
		//=======================================================
		scan.setFilter(rf);
		
		ResultScanner rss = table.getScanner(scan);
		for(Result r : rss){
			byte[] bs_name = r.getValue(Bytes.toBytes("base_info"),Bytes.toBytes("name"));
			System.out.println(new String(bs_name));
		}
		
	}
	
	
	//列名的匹配条件      base_info下name列       条件 以na开头的列名  过滤出来
	//类似于   selet na开头的列    from  t_user  where na开头的列=true
	@Test
	public void scanDatafilterByColumnName() throws Exception{
		Configuration conf = HBaseConfiguration.create();
		conf.set("hbase.zookeeper.quorum", "min1");
		
		HTable table = new HTable(conf, "t_user");
		
		Scan scan = new Scan();
//==========================过滤条件======列名以”na“开头的========返回的值  只有na开头的这个列的内容==其他列的内容是没有的==============================
		ColumnPrefixFilter cpf = new ColumnPrefixFilter(Bytes.toBytes("na"));
//==========================================================================================
		scan.setFilter(cpf);
		
		ResultScanner rs = table.getScanner(scan);
		
		for(Result rt : rs){
			byte[] bt_hobby = rt.getValue(Bytes.toBytes("base_info"),Bytes.toBytes("age"));
			if(bt_hobby!=null){
				System.out.println(new String(bt_hobby));
			}
		}
		
	}
	
	//以上条件所有的过滤条件，都是单一的过滤条件，是否可以有多个条件组合到一起
	//类似于  where  age>10   and   hobby="football"   这是两个过滤条件
	@Test
	public void testScanDataByFilterGroup() throws Exception{
		
		Configuration conf = HBaseConfiguration.create();
		conf.set("hbase.zookeeper.quorum", "min1");
		
		HTable table = new HTable(conf, "t_user");
		Scan scan = new Scan();
		
		
		//================================================================
		//条件组	条件之间的逻辑关系指定为 "与”（and）
//		FilterList fList = new FilterList(Operator.MUST_PASS_ALL);
	
		//条件之间的逻辑关系指定为 "或”（or）
		FilterList fList = new FilterList(Operator.MUST_PASS_ONE);
		//条件1    某个列的值
		SingleColumnValueFilter scvf = new SingleColumnValueFilter(
				Bytes.toBytes("base_info"), 
				Bytes.toBytes("age"), 
				CompareOp.EQUAL, 
				Bytes.toBytes("18"));
		//条件2   过滤rowkey   以bj开头的rowKey
		RowFilter rf = new RowFilter(CompareFilter.CompareOp.EQUAL,new RegexStringComparator("bj+"));
		fList.addFilter(scvf);
		fList.addFilter(rf);
		//================================================================
		
		scan.setFilter(fList);
		
		ResultScanner rs = table.getScanner(scan);
		
		for(Result r : rs){
			byte[] bs_name = r.getValue(Bytes.toBytes("base_info"), Bytes.toBytes("name"));
			
			if(bs_name!=null){
				System.out.println(new String(bs_name));
			}
			
			byte[] bs_age = r.getValue(Bytes.toBytes("base_info"), Bytes.toBytes("age"));
			
			if(bs_age!=null){
				System.out.println(new String(bs_age));
			}
		}
		table.close();
	}
	
	//删除某一记录（行）
	@Test
	public void testDelRow() throws Exception{
		Configuration conf = HBaseConfiguration.create();
		conf.set("hbase.zookeeper.quorum", "min1");
		HTable table = new HTable(conf, "t_user");
				
		Delete delete = new Delete(Bytes.toBytes("sz_0004"));
		
		table.delete(delete);
		table.close();
	}
	
	//删除表
	@Test
	public void testDropTable() throws Exception{
		Configuration conf = HBaseConfiguration.create();
		conf.set("hbase.zookeeper.quorum", "min1");
		Connection connection = ConnectionFactory.createConnection(conf);
		
		Admin admin = connection.getAdmin();
		TableName tn = TableName.valueOf("t_user");
		admin.disableTable(tn);
		
		admin.deleteTable(tn);
		admin.close();
		
	}
	
	
	
	
	/**
	 * 6.多种过滤条件的使用方法
	 */
	@Test
	public void testScan() throws Exception{
		Configuration conf = HBaseConfiguration.create();
		conf.set("hbase.zookeeper.quorum", "min1");
		HTable table = new HTable(conf, "t_user".getBytes());
		//参数1  startRow  参数2  endRow   类似limit
		Scan scan = new Scan(Bytes.toBytes("bj_0001"), Bytes.toBytes("bj_0002"));
		
		//前缀过滤器----针对行键
Filter filter = new PrefixFilter(Bytes.toBytes("bj"));
		
		//行过滤器  过滤条件
ByteArrayComparable rowComparator = new BinaryComparator(Bytes.toBytes("bj_0001"));
RowFilter rf = new RowFilter(CompareOp.LESS_OR_EQUAL, rowComparator);
		
		/**
         * 假设rowkey格式为：创建日期_发布日期_ID_TITLE
         * 目标：查找  发布日期  为  2014-12-21  的数据
         */
        rf = new RowFilter(CompareOp.EQUAL , new SubstringComparator("_2014-12-21_"));
		
		
		//单值过滤器 1 完整匹配字节数组
		new SingleColumnValueFilter("base_info".getBytes(), "name".getBytes(), CompareOp.EQUAL, "zhangsan".getBytes());
		//单值过滤器2 匹配正则表达式
		ByteArrayComparable comparator = new RegexStringComparator("zhang.");
		new SingleColumnValueFilter("info".getBytes(), "NAME".getBytes(), CompareOp.EQUAL, comparator);

		//单值过滤器2 匹配是否包含子串,大小写不敏感
		comparator = new SubstringComparator("wu");
		new SingleColumnValueFilter("info".getBytes(), "NAME".getBytes(), CompareOp.EQUAL, comparator);

		//键值对元数据过滤-----family过滤----字节数组完整匹配
        FamilyFilter ff = new FamilyFilter(
                CompareOp.EQUAL , 
                new BinaryComparator(Bytes.toBytes("base_info"))   //表中不存在inf列族，过滤结果为空
                );
        //键值对元数据过滤-----family过滤----字节数组前缀匹配
        ff = new FamilyFilter(
                CompareOp.EQUAL , 
                new BinaryPrefixComparator(Bytes.toBytes("inf"))   //表中存在以inf打头的列族info，过滤结果为该列族所有行
                );
		
        
       //键值对元数据过滤-----qualifier过滤----字节数组完整匹配
        filter = new QualifierFilter(
                CompareOp.EQUAL , 
                new BinaryComparator(Bytes.toBytes("na"))   //表中不存在na列，过滤结果为空
                );
        filter = new QualifierFilter(
                CompareOp.EQUAL , 
                new BinaryPrefixComparator(Bytes.toBytes("na"))   //表中存在以na打头的列name，过滤结果为所有行的该列数据
        		);
		
        //基于列名(即Qualifier)前缀过滤数据的ColumnPrefixFilter
        filter = new ColumnPrefixFilter("na".getBytes());
        
        //基于列名(即Qualifier)多个前缀过滤数据的MultipleColumnPrefixFilter
        byte[][] prefixes = new byte[][] {Bytes.toBytes("na"), Bytes.toBytes("me")};
        filter = new MultipleColumnPrefixFilter(prefixes);
 
        //为查询设置过滤条件
        scan.setFilter(filter);
        
        
		scan.addFamily(Bytes.toBytes("base_info"));
		ResultScanner scanner = table.getScanner(scan);
		for(Result r : scanner){
			/**
			for(KeyValue kv : r.list()){
				String family = new String(kv.getFamily());
				System.out.println(family);
				String qualifier = new String(kv.getQualifier());
				System.out.println(qualifier);
				System.out.println(new String(kv.getValue()));
			}
			*/
			//直接从result中取到某个特定的value
			byte[] value = r.getValue(Bytes.toBytes("base_info"), Bytes.toBytes("name"));
			System.out.println(new String(value));
		}
		table.close();
	}
	
}







































