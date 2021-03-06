package org.nutz.plugins.ioc.loader.dao;

import java.util.List;
import java.util.Map;

import org.nutz.dao.Cnd;
import org.nutz.dao.Dao;
import org.nutz.dao.entity.Record;
import org.nutz.ioc.IocLoading;
import org.nutz.ioc.ObjectLoadException;
import org.nutz.ioc.loader.json.JsonLoader;
import org.nutz.ioc.meta.IocObject;
import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.mvc.Mvcs;

/**
 * <p>从数据库加载ioc配置, 需要4个参数,
 * Dao在iocbean中的名字(默认为dao),
 * 表的名词(默认为t_iocbean),
 * bean的名称对应的字段名(默认是nm),
 * 配置对应在的字段名(默认是val). </p>
 * <code>
 * select val from t_iocbean where nm="mqtt"
 * </code>
 * @author wendal(wendal1985@gmail.com)
 *
 */
public class DaoIocLoader extends JsonLoader {

    protected String name = "dao";
    protected String table = "t_iocbean";
    protected String nameField = "nm";
    protected String valueField = "val";
    protected Dao dao;
    public DaoIocLoader(String ... args) {
        if (args.length > 0) {
            name = args[0];
            if (args.length > 1) {
                table = args[1];
                if (args.length > 2) {
                    nameField = args[2];
                    if (args.length > 3) {
                        valueField = args[3];
                    }
                }
            }
        }
    }
    
    public IocObject load(IocLoading loading, String name) throws ObjectLoadException {
        if (getMap().containsKey(name))
            return super.load(loading, name);
        Record re = dao.fetch(table, Cnd.where(nameField, "=", name));
        if (re == null)
            throw new ObjectLoadException("Object '" + name + "' without define!");
        Map<String, Object> map = Json.fromJsonAsMap(Object.class, re.getString(valueField));
        try {
            map.put(name, map);
            return super.load(loading, name);
        } catch (Throwable e) {
            map.remove(name);
            throw Lang.wrapThrow(e);
        }
    }
    
    public boolean has(String name) {
        return 0 != dao().count(table, Cnd.where(nameField, "=", name));
    }
    
    public String[] getName() {
        List<Record> res =  dao().query(table, null);
        if (res == null || res.isEmpty())
            return new String[0];
        String[] names = new String[res.size()];
        for (int i = 0; i < res.size(); i++) {
            names[i] = res.get(i).getString(nameField);
        }
        return names;
    }
    
    protected Dao dao() {
        if (dao == null)
            dao = Mvcs.getIoc().get(Dao.class, name);
        return dao;
    }
}
