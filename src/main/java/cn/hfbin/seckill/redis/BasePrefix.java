package cn.hfbin.seckill.redis;

public abstract class BasePrefix implements KeyPrefix{


	private String prefix;

	public BasePrefix( String prefix) {
		this.prefix = prefix;
	}

	public String getPrefix() {
		//这里的getClass应该是继承BasePrefix的其他prefix的类
		String className = getClass().getSimpleName();
		return className+":" + prefix;
	}

}
