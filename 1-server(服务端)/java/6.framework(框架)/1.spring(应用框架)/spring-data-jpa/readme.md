#### 1.ID生成策略

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private String id;
    
    @GeneratedValue源码里可以看到，strategy属性是由GenerationType指定的，我们点进 GenerationType里面可以看到这里定义了四种策略： 
        - TABLE：使用一个特定的数据库表格来保存主键。 
        - SEQUENCE：根据底层数据库的序列来生成主键，条件是数据库支持序列。 
        - IDENTITY：主键由数据库自动生成（主要是自动增长型） 
        - AUTO：主键由程序控制(也是默认的,在指定主键时，如果不指定主键生成策略，默认为AUTO) 
 
 #### 2.索引定义
 
    @Table(name="t_data_draw",uniqueConstraints=@UniqueConstraint(columnNames={"uuid"}))
    
    实例:
    @Table(name="t_data_draw",uniqueConstraints=@UniqueConstraint(columnNames={"uuid"}))
    @TableName("t_data_draw")
    @ApiModel(value = "绘图")
    public class DrawData extends KbsBaseEntity {
    
        private String uuid;
    
        private String ownerId;
    
        private String type;
    
        private String name;
    
        private String body;
    
        private String attachment;
    
        private String relateCode;
    
        private String attribute;
    
    
    }
##### 3.定义blob属性
     //@Lob 通常与@Basic同时使用，提高访问速度
     @Lob
     @Basic(fetch = FetchType.LAZY)
     @Column(name=" body", columnDefinition="longblob", nullable=true)
     private byte[] body;
     
