package org.tholian.rewrite.annotations;

import static org.openrewrite.java.Assertions.java;

import org.junit.jupiter.api.Test;

import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

class SortAnnotationsTest implements RewriteTest {

	@Override
	public void defaults( RecipeSpec spec ) {
		spec.recipe( new SortAnnotations( ) );
	}

	@Test
	void sortsAnnotationsOnClassMethodAndField( ) {
		rewriteRun(
				java(
						// language=java
						"""
								@B
								@A
								class Test {
								    @D
								    @C
								    void m() {
								    }
								
								    @F
								    @A
								    @E
								    int x;
								
									@C
									@Override
									@A
									public String toString() {
										return super.toString();
									}
								}
								
								@interface A {}
								@interface B {}
								@interface C {}
								@interface D {}
								@interface E {}
								@interface F {}
								""",
						// language=java
						"""
								@A
								@B
								class Test {
								    @C
								    @D
								    void m() {
								    }
								
								    @A
								    @E
								    @F
								    int x;
								
									@A
									@C
									@Override
									public String toString() {
										return super.toString();
									}
								}
								
								@interface A {}
								@interface B {}
								@interface C {}
								@interface D {}
								@interface E {}
								@interface F {}
								"""
				)
		);
	}

	@Test
	void canDisableMethodSorting( ) {
		rewriteRun(
				spec -> spec.recipe( new SortAnnotations( true, false, true ) ),
				java(
						// language=java
						"""
								@B
								@A
								class Test {
								    @D
								    @C
								    void m() {
								    }
								
								    @F
								    @Ab
								    @A
								    @E
								    int x;
								
									@C
									@Override
									@A
									public String toString() {
										return super.toString();
									}
								}
								
								@interface A {}
								@interface Ab {}
								@interface B {}
								@interface C {}
								@interface D {}
								@interface E {}
								@interface F {}
								""",
						// language=java
						"""
								@A
								@B
								class Test {
								    @D
								    @C
								    void m() {
								    }
								
								    @A
								    @Ab
								    @E
								    @F
								    int x;
								
									@C
									@Override
									@A
									public String toString() {
										return super.toString();
									}
								}
								
								@interface A {}
								@interface Ab {}
								@interface B {}
								@interface C {}
								@interface D {}
								@interface E {}
								@interface F {}
								"""
				)
		);
	}
}
