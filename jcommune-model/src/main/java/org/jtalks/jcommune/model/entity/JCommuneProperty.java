/**
 * Copyright (C) 2011  JTalks.org Team
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.jtalks.jcommune.model.entity;

import org.jtalks.common.model.entity.Component;
import org.jtalks.common.model.entity.Property;
import org.jtalks.jcommune.model.dao.ComponentDao;
import org.jtalks.jcommune.model.dao.PropertyDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Provides access to the JCommune property, which is stored in the database.
 * Each enum value is wired as a separate bean to inject individual
 * properties into other beans.
 *
 * @author Anuar_Nurmakanov
 */
public enum JCommuneProperty {
    /**
     * The property to check the enabling of email notifications to subscribers of topics or branches.
     */
    SENDING_NOTIFICATIONS_ENABLED,

    /**
     * Property for session timeout for logged users.
     */
    SESSION_TIMEOUT,

    /**
     * Property for the maximum size of the avatar.
     */
    AVATAR_MAX_SIZE,

    /**
     * Name of component
     */
    CMP_NAME,

    /**
     * Description of component
     */
    CMP_DESCRIPTION;


    private static final Logger LOGGER = LoggerFactory.getLogger(JCommuneProperty.class);

    private String name;
    private String defaultValue;
    private PropertyDao propertyDao;
    private ComponentDao componentDao;

    /**
     * Returns a string value of the property. Property values
     * ​​are stored as strings, so this method is main for retrieving
     * a value of property.
     * It is also worth noting that if the property has not been found,
     * it will return a default value.
     *
     * @return a string value of the property
     */
    public String getValue() {

        if (propertyDao != null) {
            Property property = propertyDao.getByName(name);
            if (property != null) {
                return property.getValue();
            } else {
                LOGGER.warn("{} property was not found, using default value {}", name, defaultValue);
                return defaultValue;
            }
        } else {
            LOGGER.warn("{} property was not found, using default value {}", name, defaultValue);
            return defaultValue;
        }
    }

    /**
     * Returns a string value of the component property.
     * It is also worth noting that if the property has not been found,
     * it will return a default value.
     *
     * @return a string value of component property
     */
    public String getValueOfComponent() {
        if (componentDao != null) {
            Component cmp = componentDao.getComponent();
            if (cmp != null) {
                return name.equals("cmp.name") ? cmp.getName() : cmp.getDescription();
            } else {
                LOGGER.warn("{} name of component was not found, using default value {}", name, defaultValue);
                return defaultValue;
            }
        } else {
            LOGGER.warn("{} name of component was not found, using default value {}", name, defaultValue);
            return defaultValue;
        }
    }

    /**
     * Converts a value of the property to boolean and returns it.
     * Keep in mind, if the property isn't boolean, the result will false.
     *
     * @return a boolean value of the property
     */
    public boolean booleanValue() {
        return Boolean.valueOf(getValue());
    }

    /**
     * Converts a value of the property to int and returns it.
     * Keep in mind, if the property isn't integer {@link NumberFormatException}
     * will be thrown
     *
     * @return a boolean value of the property
     */
    public int intValue() {
        return Integer.valueOf(getValue());
    }

    /**
     * @param name the name of the property
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param defaultValue default value for current property
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * Set an instance of {@link org.jtalks.jcommune.model.dao.PropertyDao} to search properties by name.
     *
     * @param propertyDao an instance of {@link org.jtalks.jcommune.model.dao.PropertyDao}
     */
    public void setPropertyDao(PropertyDao propertyDao) {
        this.propertyDao = propertyDao;
    }

    /**
     * Set an instance of {@link org.jtalks.jcommune.model.dao.ComponentDao} to search properties of Component.
     *
     * @param componentDao an instance of {@link org.jtalks.jcommune.model.dao.ComponentDao}
     */
    public void setComponentDao(ComponentDao componentDao) {
        this.componentDao = componentDao;
    }
}
