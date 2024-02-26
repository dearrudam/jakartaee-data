/*
 * Copyright (c) 2023,2024 Contributors to the Eclipse Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package jakarta.data.repository;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Lifecycle annotation for repository methods which perform update operations.</p>
 *
 * <p>The {@code Update} annotation indicates that the annotated repository method updates the state of one or more
 * entities already held in the database. The annotated repository method usually has exactly one parameter whose type
 * is one of the following:
 * </p>
 * <ul>
 *     <li>The entity to be updated.</li>
 *     <li>An {@code Iterable} of entities to be updated.</li>
 *     <li>An array of entities to be updated.</li>
 * </ul>
 * <p>The annotated method must either be declared {@code void}, or have a return type that is the same as the type of
 * its parameter.
 * </p>
 * <p>For example, consider an interface representing a garage:</p>
 * <pre>
 * {@code @Repository}
 * interface Garage {
 *     {@code @Update}
 *     Car update(Car car);
 * }
 * </pre>
 * <p>When the annotated method is non-{@code void}, it must return an updated entity instance for each entity instance
 * passed as an argument. Instances returned by the annotated method must include all values that were written to the
 * database, including all automatically generated values, updated versions, and incremented values which changed as a
 * result of the update. The order of entities within an {@code Iterable} or array return value must match the position
 * of entities in the argument, based on the unique identifier of the entity. After the annotated method returns, an
 * original entity instance supplied as an argument might not accurately reflect the updated state.
 * </p>
 * <p>Updates are performed by matching the unique identifier of the entity. If the entity is versioned, for example,
 * with {@code jakarta.persistence.Version}, the version is also checked for consistency. Attributes other than the
 * identifier and version do not need to match. If no entity with a matching identifier is found in the database, or
 * if the entity with a matching identifier does not have a matching version, the annotated method must raise
 * {@link jakarta.data.exceptions.OptimisticLockingFailureException}.
 * </p>
 * <p>
 * If the database follows the BASE model, or uses an append model to write data, the annotated method behaves the same
 * as the {@code @Insert} method.
 * </p>
 * <p>Application of the {@code Update} annotation to a method with any other signature is not portable between Jakarta
 * Data providers.
 * </p>
 * <p>Annotations such as {@code @Find}, {@code @Query}, {@code @Insert}, {@code @Update}, {@code @Delete}, and
 * {@code @Save} are mutually-exclusive. A given method of a repository interface may have at most one {@code @Find}
 * annotation, lifecycle annotation, or query annotation.
 * </p
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Update {
}
