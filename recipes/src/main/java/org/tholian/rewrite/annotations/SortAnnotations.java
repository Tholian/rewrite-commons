package org.tholian.rewrite.annotations;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import org.jspecify.annotations.NonNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Space;

@Value
@EqualsAndHashCode( callSuper = false )
@Slf4j
public class SortAnnotations extends Recipe {

	@Option( displayName = "Sortiere Klassen",
			description = "Sortiert Annotationen an Klassen, Interfaces und Enums.",
			required = false )
	Boolean sortClasses;

	@Option( displayName = "Sortiere Methoden",
			description = "Sortiert Annotationen an Methodenkonstruktoren.",
			required = false )
	Boolean sortMethods;

	@Option( displayName = "Sortiere Felder",
			description = "Sortiert Annotationen an Klassenfeldern und lokalen Variablen.",
			required = false )
	Boolean sortFields;

	// Standard-Konstruktor für OpenRewrite (nutzt Standardwerte)
	public SortAnnotations( ) {
		this.sortClasses = true;
		this.sortMethods = true;
		this.sortFields = true;
	}

	// Konstruktor für Tests oder explizite Instanziierung
	public SortAnnotations( Boolean sortClasses, Boolean sortMethods, Boolean sortFields ) {
		this.sortClasses = sortClasses != null ? sortClasses : true;
		this.sortMethods = sortMethods != null ? sortMethods : true;
		this.sortFields = sortFields != null ? sortFields : true;
	}

	@Override
	public @NonNull String getDisplayName( ) {
		return "Sortiere Annotationen alphabetisch";
	}

	@Override
	public @NonNull String getDescription( ) {
		return "Sortiert Annotationen alphabetisch an Klassen, Methoden und Feldern, konfigurierbar.";
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor( ) {
		return new JavaIsoVisitor<ExecutionContext>( ) {

			@Override
			public J.ClassDeclaration visitClassDeclaration( J.ClassDeclaration classDecl, ExecutionContext ctx ) {
				J.ClassDeclaration cd = super.visitClassDeclaration( classDecl, ctx );
				if ( Boolean.TRUE.equals( sortClasses ) ) {
					log.debug( "Sortiere Annotationen an Klasse {}", cd.getType( ).getFullyQualifiedName( ) );
					List<J.Annotation> sorted = sortList( cd.getLeadingAnnotations( ) );
					if ( sorted != null ) {
						return cd.withLeadingAnnotations( sorted );
					}
				}
				return cd;
			}

			@Override
			public J.MethodDeclaration visitMethodDeclaration( J.MethodDeclaration method, ExecutionContext ctx ) {
				J.MethodDeclaration md = super.visitMethodDeclaration( method, ctx );
				if ( Boolean.TRUE.equals( sortMethods ) ) {
					log.debug( "Sortiere Annotationen an Methode {}", md.getName( ).getSimpleName( ) );
					List<J.Annotation> sorted = sortList( md.getLeadingAnnotations( ) );
					if ( sorted != null ) {
						return md.withLeadingAnnotations( sorted );
					}
				}
				return md;
			}

			@Override
			public J.VariableDeclarations visitVariableDeclarations( J.VariableDeclarations multiVariable, ExecutionContext ctx ) {
				J.VariableDeclarations vd = super.visitVariableDeclarations( multiVariable, ctx );
				// Behandelt Felder (Fields) und lokale Variablen
				if ( Boolean.TRUE.equals( sortFields ) ) {
					log.debug( "Sortiere Annotationen an Feld {}", vd.getVariables( ).get( 0 ).getName( ).getSimpleName( ) );
					List<J.Annotation> sorted = sortList( vd.getLeadingAnnotations( ) );
					if ( sorted != null ) {
						return vd.withLeadingAnnotations( sorted );
					}
				}
				return vd;
			}
		};
	}

	/**
	 * Hilfsmethode zum Sortieren einer Liste von Annotationen.
	 * Gibt null zurück, wenn keine Änderung notwendig ist.
	 */
	private List<J.Annotation> sortList( List<J.Annotation> originalAnnotations ) {
		if ( originalAnnotations == null || originalAnnotations.size( ) <= 1 ) {
			return null;
		}

		List<Space> originalPrefixes = new ArrayList<>( originalAnnotations.size( ) );
		for ( J.Annotation annotation : originalAnnotations ) {
			originalPrefixes.add( annotation.getPrefix( ) );
		}

		List<J.Annotation> sortedAnnotations = new ArrayList<>( originalAnnotations );
		// Sortiert basierend auf dem String-Namen der Annotation (z.B. "@Alpha" vor "@Beta")
		sortedAnnotations.sort( Comparator.comparing( a -> a.getAnnotationType( ).toString( ) ) );

		boolean sameOrder = true;
		for ( int i = 0; i < originalAnnotations.size( ); i++ ) {
			String originalName = originalAnnotations.get( i ).getAnnotationType( ).toString( );
			String sortedName = sortedAnnotations.get( i ).getAnnotationType( ).toString( );
			if ( !originalName.equals( sortedName ) ) {
				sameOrder = false;
				break;
			}
		}
		if ( sameOrder ) {
			return null;
		}

		List<J.Annotation> normalized = new ArrayList<>( sortedAnnotations.size( ) );
		for ( int i = 0; i < sortedAnnotations.size( ); i++ ) {
			normalized.add( sortedAnnotations.get( i ).withPrefix( originalPrefixes.get( i ) ) );
		}

		return normalized;
	}
}
