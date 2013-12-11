package io.github.ibuildthecloud.dstack.object.jooq.utils;

import io.github.ibuildthecloud.gdapi.factory.SchemaFactory;
import io.github.ibuildthecloud.gdapi.model.Schema;

import org.jooq.DSLContext;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableRecord;
import org.jooq.UniqueKey;
import org.jooq.UpdatableRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JooqUtils {

    private static final Logger log = LoggerFactory.getLogger(JooqUtils.class);

    public static <T> T findById(DSLContext context, Class<T> clz, Object id) {
        if ( id == null )
            return null;

        Table<?> table = getTable(clz);
        if ( table == null )
            return null;

        UniqueKey<?> key = table.getPrimaryKey();
        if ( key == null || key.getFieldsArray().length != 1 )
            return null;

        @SuppressWarnings("unchecked")
        TableField<?, Object> keyField = (TableField<?, Object>)key.getFieldsArray()[0];

        /* Convert object because we are abusing type safety here */
        Object converted = keyField.getDataType().convert(id);

        return context.select()
                .from(table)
                .where(keyField.eq(converted))
                .fetchOneInto(clz);
    }

    @SuppressWarnings("unchecked")
    public static Table<?> getTable(Class<?> clz) {
        if ( clz == null )
            return null;

        if ( TableRecord.class.isAssignableFrom(clz) ) {
            try {
                TableRecord<?> record =
                        ((Class<TableRecord<?>>)clz).newInstance();
                return record.getTable();
            } catch (InstantiationException e) {
                log.error("Failed to determine table for [{}]", clz, e);
            } catch (IllegalAccessException e) {
                log.error("Failed to determine table for [{}]", clz, e);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static Class<UpdatableRecord<?>> getRecordClass(SchemaFactory factory, Class<?> clz) {
        if ( UpdatableRecord.class.isAssignableFrom(clz) ) {
            return (Class<UpdatableRecord<?>>)clz;
        }

        if ( factory != null ) {
            Schema schema = factory.getSchema(clz);
            Class<?> testClz = factory.getSchemaClass(schema.getId());
            if ( clz.isAssignableFrom(testClz) ) {
                if ( ! UpdatableRecord.class.isAssignableFrom(testClz) ) {
                    throw new IllegalArgumentException("Class [" + testClz + "] is not an instanceof UpdatableRecord");
                }
                return (Class<UpdatableRecord<?>>) testClz;
            }
        }

        throw new IllegalArgumentException("Failed to find UpdatableRecord class for [" + clz + "]");
    }

    public static UpdatableRecord<?> getRecord(Class<UpdatableRecord<?>> clz) {
        try {
            return clz.newInstance();
        } catch (InstantiationException e) {
            throw new IllegalStateException("Failed to instantiate [" + clz + "]", e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to instantiate [" + clz + "]", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends UpdatableRecord<?>> T getRecordObject(Object obj) {
        if ( obj == null )
            return null;

        if ( obj instanceof UpdatableRecord<?> ) {
            return (T)obj;
        }
        throw new IllegalArgumentException("Expected instance of [" + UpdatableRecord.class + "] got [" + obj.getClass() + "]");
    }

}