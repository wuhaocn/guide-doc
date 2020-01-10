#### java自动化操作git

##### 一、简介

    jgit是存java实现的git版本控制，学习jgit可以更好的理解学习git，其源代码托管在github上JGit。主要的模块如下：
    
    org.eclipse.jgit 核心实现，包括git命令、协议等
    org.eclipse.jgit.archive 支持导出各种压缩的格式
    org.eclipse.jgit.http.server 支持http协议的服务器，主要提供GitServlet
    使用JGit的软件有：EGit（Eclipse git版本管理插件）、Gitblit等。其maven配置坐标为：
    maven:
    <dependency>
            <groupId>org.eclipse.jgit</groupId>
            <artifactId>org.eclipse.jgit</artifactId>
            <version>5.2.1.201812262042-r</version>
    </dependency>
    
    gradle:
    compile group: 'org.eclipse.jgit', name: 'org.eclipse.jgit', version: '5.2.1.201812262042-r'
##### 二、API使用

###### 1.基本概念


    Repository 包括所有的对象和引用，用来管理源码
    AnyObjectId 表示SHA1对象，可以获得SHA1的值，进而可以获得git对象
    Ref 引用对象，表示.git/refs下面的文件引用 Ref HEAD = repository.getRef("refs/heads/master");
    RevWalk 可以遍历提交对象，并按照顺序返回提交对象
    RevCommit 代表一个提交对象
    RevTag 代表标签对象
    RevTree 代表树对象

###### 2. 创建本地仓库
     
    初始化并创建配置厂库
    public static void init(String baseDirStr){
        try {
            File baseDir = new File(baseDirStr + "/.git");
            Git.init().setGitDir(baseDir).setDirectory(baseDir.getParentFile()).call();
        } catch (Exception e) {
            log.error("init Exception:{}", baseDirStr, e);
        }
    }
    只需要简单的一行代码就可以创建本地仓库，可以调用setBare方法来设置版本库是否为裸版本库。
    注意org.eclipse.jgit.api.Git这个类，其方法返回所有支持的git命令
    git学习之jgit
    另外对于所有的命令，使用Builder模式。
    
3.配置本地仓库

    Repository build = null;
        try {
            build = new RepositoryBuilder().setGitDir(new File("D:\\source-code\\temp\\.git")).setMustExist(true)
                    .build();
            build.getConfig().setString(ConfigConstants.CONFIG_USER_SECTION, null, ConfigConstants.CONFIG_KEY_NAME,
                    "xxxx");
            build.getConfig().save();
    
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (null != build) {              //为了节省篇幅，后续代码省略调Git对象和Repository对象的close方法
                build.close();
            }
        }
    设置完以后可以到.git/config目录下面看到配置生效。版本库api并没有直接提供修改系统或者用户级别的配置（--system、--global），如果用户需要修改这两个级别的，可以参考FileRepository 的构造函数：
    
    if (StringUtils.isEmptyOrNull(SystemReader.getInstance().getenv(
                    Constants.GIT_CONFIG_NOSYSTEM_KEY)))
                systemConfig = SystemReader.getInstance().openSystemConfig(null,
                        getFS());
            else
                systemConfig = new FileBasedConfig(null, FS.DETECTED) {
                    @Override
                    public void load() {
                        // empty, do not load
                    }
    
                    @Override
                    public boolean isOutdated() {
                        // regular class would bomb here
                        return false;
                    }
                };
            userConfig = SystemReader.getInstance().openUserConfig(systemConfig,
                    getFS());
    
            loadSystemConfig();        //load完以后，可以直接修改config对象了，最后save一下就可以了
            loadUserConfig();
4.修改本地仓库
    
    AddCommand可以把工作区的内容添加到暂存区。
    
    Git git = Git.open(new File("D:\\source-code\\temp\\.git"));
    git.add().addFilepattern(".").call(); // 相当与git add -A添加所有的变更文件git.add().addFilepattern("*.java")这种形式是不支持的
    git.add().addFilepattern("src/main/java/").call(); // 添加目录，可以把目录下的文件都添加到暂存区
    //jgit当前还不支持模式匹配的方式，例如*.java
    CommitCommand用于提交操作
    
    Git git =Git.open(new File("D:\\source-code\\temp\\user1\\.git"));
    CommitCommand commitCommand = git.commit().setMessage("master 23 commit").setAllowEmpty(true);
    commitCommand.call();
    5.查看本地仓库
    
    StatusCommand命令等同于git status命令
    
        Git git = Git.open(new File("D:\\source-code\\temp-1\\.git"));
        Status status = git.status().call();        //返回的值都是相对工作区的路径，而不是绝对路径
        status.getAdded().forEach(it -> System.out.println("Add File :" + it));      //git add命令后会看到变化
        status.getRemoved().forEach(it -> System.out.println("Remove File :" + it));  ///git rm命令会看到变化，从暂存区删除的文件列表
        status.getModified().forEach(it -> System.out.println("Modified File :" + it));  //修改的文件列表
        status.getUntracked().forEach(it -> System.out.println("Untracked File :" + it)); //工作区新增的文件列表
        status.getConflicting().forEach(it -> System.out.println("Conflicting File :" + it)); //冲突的文件列表
        status.getMissing().forEach(it -> System.out.println("Missing File :" + it));    //工作区删除的文件列表
    LogCommand相当于git log命令

    //提取某个作者的提交，并打印相关信息
    Git git = Git.open(new File("D:\\source-code\\temp-1\\.git"));
    DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Iterable<RevCommit> results = git.log().setRevFilter(new RevFilter() {
        @Override
        public boolean include(RevWalk walker, RevCommit cmit)
           throws StopWalkException, MissingObjectException, IncorrectObjectTypeException, IOException {
            return cmit.getAuthorIdent().getName().equals("xxxxx dsd");
        }
    
        @Override
        public RevFilter clone() {
        return this;
                }
            }).call();
    results.forEach(commit -> {
        PersonIdent authoIdent = commit.getAuthorIdent();
        System.out.println("提交人：  " + authoIdent.getName() + "     <" + authoIdent.getEmailAddress() + ">");
        System.out.println("提交SHA1：  " + commit.getId().name());
        System.out.println("提交信息：  " + commit.getShortMessage());
        System.out.println("提交时间：  " + format.format(authoIdent.getWhen()));
    });
5.远程代码库命令

    1.clone命令
    
    CloneCommand等价与git clone命令
    
    Git.cloneRepository().setURI("https://admin@localhost:8443/r/game-of-life.git")
                    .setDirectory(new File("D:\\source-code\\temp-1")).call();
    2.push、fetch命令
    
    Repository rep = new FileRepository("D:\\source-code\\temp-1\\.git");
    Git git = new Git(rep);
    git.pull().setRemote("origin").call();
    //fetch命令提供了setRefSpecs方法，而pull命令并没有提供，所有pull命令只能fetch所有的分支
    git.fetch().setRefSpecs("refs/heads/*:refs/heads/*").call();
    而PushCommand和git push相同，一般都需要我们提供用户名和密码，需要用到CredentialsProvider类
    
    Repository rep = new FileRepository("D:\\source-code\\temp-1\\.git");
    Git git = new Git(rep);
    git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider("myname", "password")).call();
    6.RevWalk API
    
    RevWalk基本是jgit中使用最多的api了，很多jgit的命令就是使用该api实现的。
    我们当前实现这样一个功能，查找某个文件的历史记录，并把每个提交的文件内容打印出来。
    
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Repository repository = new RepositoryBuilder().setGitDir(new File("D:\\source-code\\temp-1\\.git")).build();
            try (RevWalk walk = new RevWalk(repository)) {
                Ref head = repository.findRef("HEAD");
                walk.markStart(walk.parseCommit(head.getObjectId())); // 从HEAD开始遍历，
                for (RevCommit commit : walk) {
                    RevTree tree = commit.getTree();
    
                    TreeWalk treeWalk = new TreeWalk(repository, repository.newObjectReader());
                    PathFilter f = PathFilter.create("pom.xml");
                    treeWalk.setFilter(f);
                    treeWalk.reset(tree);
                    treeWalk.setRecursive(false);
                    while (treeWalk.next()) {
                        PersonIdent authoIdent = commit.getAuthorIdent();
                        System.out.println("提交人： " + authoIdent.getName() + " <" + authoIdent.getEmailAddress() + ">");
                        System.out.println("提交SHA1： " + commit.getId().name());
                        System.out.println("提交信息： " + commit.getShortMessage());
                        System.out.println("提交时间： " + format.format(authoIdent.getWhen()));
    
                        ObjectId objectId = treeWalk.getObjectId(0);
                        ObjectLoader loader = repository.open(objectId);
                        loader.copyTo(System.out);              //提取blob对象的内容
                    }
                }
            }
7.参考资料

    jgit-cookbook
    
    https://blog.51cto.com/5162886/2094475