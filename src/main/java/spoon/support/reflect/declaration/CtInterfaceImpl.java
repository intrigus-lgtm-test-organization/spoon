/*
 * SPDX-License-Identifier: (MIT OR CECILL-C)
 *
 * Copyright (C) 2006-2019 INRIA and contributors
 *
 * Spoon is available either under the terms of the MIT License (see LICENSE-MIT.txt) of the Cecill-C License (see LICENSE-CECILL-C.txt). You as the user are entitled to choose the terms under which to adopt Spoon.
 */
package spoon.support.reflect.declaration;

import java.util.HashSet;
import java.util.stream.Collectors;
import spoon.reflect.declaration.CtInterface;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.CtVisitor;
import spoon.support.UnsettableProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import spoon.support.reflect.CtExtendedModifier;

public class CtInterfaceImpl<T> extends CtTypeImpl<T> implements CtInterface<T> {
	private static final long serialVersionUID = 1L;

	@Override
	public void accept(CtVisitor visitor) {
		visitor.visitCtInterface(this);
	}

	@Override
	public boolean isSubtypeOf(CtTypeReference<?> type) {
		return getReference().isSubtypeOf(type);
	}

	@Override
	public boolean isInterface() {
		return true;
	}

	@Override
	public Collection<CtExecutableReference<?>> getDeclaredExecutables() {
		Set<CtTypeReference<?>> superInterfaces = getSuperInterfaces();
		if (superInterfaces.isEmpty()) {
			return super.getDeclaredExecutables();
		}
		List<CtExecutableReference<?>> l = new ArrayList<>(super.getDeclaredExecutables());
		for (CtTypeReference<?> sup : superInterfaces) {
			l.addAll(sup.getAllExecutables());
		}
		return Collections.unmodifiableList(l);
	}

	@Override
	public CtInterface<T> clone() {
		return (CtInterface<T>) super.clone();
	}

	@Override
	public <N, C extends CtType<T>> C addNestedType(CtType<N> nestedType) {
		super.addNestedType(nestedType);

		// Type members of interfaces are implicitly public static. We need to add the implicit
		// modifiers if they aren't public static already.
		Set<CtExtendedModifier> modifiers = new HashSet<>(nestedType.getExtendedModifiers());
		if (!nestedType.isPublic()) {
			modifiers.add(new CtExtendedModifier(ModifierKind.PUBLIC, true));
		}
		if (!nestedType.isStatic()) {
			modifiers.add(new CtExtendedModifier(ModifierKind.STATIC, true));
		}
		nestedType.setExtendedModifiers(modifiers);

		return (C) this;
	}

	@Override
	public <N> boolean removeNestedType(CtType<N> nestedType) {
		if (!super.removeNestedType(nestedType)) {
			return false;
		}

		// We might have added implicit public static modifiers so we need to remove them again
		Set<CtExtendedModifier> newModifiers = nestedType.getExtendedModifiers().stream()
				.filter(modifier -> {
					if (!modifier.isImplicit()) {
						return true;
					}
					return modifier.getKind() != ModifierKind.STATIC
							&& modifier.getKind() != ModifierKind.PUBLIC;
				})
				.collect(Collectors.toCollection(HashSet::new));

		nestedType.setExtendedModifiers(newModifiers);

		return true;
	}

	@Override
	@UnsettableProperty
	public <C extends CtType<T>> C setSuperclass(CtTypeReference<?> superClass) {
		// unsettable property
		return (C) this;
	}
}
