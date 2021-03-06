package monocle.syntax

import cats.{Eq, Monoid}
import monocle.function.{At, Each, Index}
import monocle.{std, Fold, Getter, Optional, PIso, PLens, POptional, PPrism, PTraversal}

case class ApplyFold[S, A](s: S, _fold: Fold[S, A]) {
  def foldMap[M: Monoid](f: A => M): M = _fold.foldMap(f)(s)

  def getAll: List[A]                       = _fold.getAll(s)
  def find(p: A => Boolean): S => Option[A] = _fold.find(p)
  def headOption: Option[A]                 = _fold.headOption(s)
  def lastOption: Option[A]                 = _fold.lastOption(s)
  def exist(p: A => Boolean): Boolean       = _fold.exist(p)(s)
  def all(p: A => Boolean): Boolean         = _fold.all(p)(s)
  def length: Int                           = _fold.length(s)
  def isEmpty: Boolean                      = _fold.isEmpty(s)
  def nonEmpty: Boolean                     = _fold.nonEmpty(s)

  def each[C](implicit evEach: Each[A, C]): ApplyFold[S, C] =
    composeTraversal(evEach.each)

  /** Select all the elements which satisfies the predicate.
    * This combinator can break the fusion property see Optional.filter for more details.
    */
  def filter(predicate: A => Boolean): ApplyFold[S, A] =
    andThen(Optional.filter(predicate))

  def some[A1](implicit ev1: A =:= Option[A1]): ApplyFold[S, A1] =
    adapt[Option[A1]] composePrism (std.option.pSome)

  def withDefault[A1: Eq](defaultValue: A1)(implicit ev1: A =:= Option[A1]): ApplyFold[S, A1] =
    adapt[Option[A1]] composeIso (std.option.withDefault(defaultValue))

  def at[I, A1](i: I)(implicit evAt: At[A, i.type, A1]): ApplyFold[S, A1] =
    composeLens(evAt.at(i))

  def index[I, A1](i: I)(implicit evIndex: Index[A, I, A1]): ApplyFold[S, A1] =
    composeOptional(evIndex.index(i))

  private def adapt[A1](implicit evA: A =:= A1): ApplyFold[S, A1] =
    evA.substituteCo[ApplyFold[S, *]](this)

  def andThen[B](other: Fold[A, B]): ApplyFold[S, B] =
    ApplyFold(s, _fold.andThen(other))
  def andThen[B](other: Getter[A, B]): ApplyFold[S, B] =
    ApplyFold(s, _fold.andThen(other))
  def andThen[B, C, D](other: PTraversal[A, B, C, D]): ApplyFold[S, C] =
    ApplyFold(s, _fold.andThen(other))
  def andThen[B, C, D](other: POptional[A, B, C, D]): ApplyFold[S, C] =
    ApplyFold(s, _fold.andThen(other))
  def andThen[B, C, D](other: PPrism[A, B, C, D]): ApplyFold[S, C] =
    ApplyFold(s, _fold.andThen(other))
  def andThen[B, C, D](other: PLens[A, B, C, D]): ApplyFold[S, C] =
    ApplyFold(s, _fold.andThen(other))
  def andThen[B, C, D](other: PIso[A, B, C, D]): ApplyFold[S, C] =
    ApplyFold(s, _fold.andThen(other))

  def composeFold[B](other: Fold[A, B]): ApplyFold[S, B]                        = andThen(other)
  def composeGetter[B](other: Getter[A, B]): ApplyFold[S, B]                    = andThen(other)
  def composeTraversal[B, C, D](other: PTraversal[A, B, C, D]): ApplyFold[S, C] = andThen(other)
  def composeOptional[B, C, D](other: POptional[A, B, C, D]): ApplyFold[S, C]   = andThen(other)
  def composePrism[B, C, D](other: PPrism[A, B, C, D]): ApplyFold[S, C]         = andThen(other)
  def composeLens[B, C, D](other: PLens[A, B, C, D]): ApplyFold[S, C]           = andThen(other)
  def composeIso[B, C, D](other: PIso[A, B, C, D]): ApplyFold[S, C]             = andThen(other)

  /** alias to composeTraversal */
  @deprecated("use andThen", since = "3.0.0-M1")
  def ^|->>[B, C, D](other: PTraversal[A, B, C, D]): ApplyFold[S, C] = andThen(other)

  /** alias to composeOptional */
  @deprecated("use andThen", since = "3.0.0-M1")
  def ^|-?[B, C, D](other: POptional[A, B, C, D]): ApplyFold[S, C] = andThen(other)

  /** alias to composePrism */
  @deprecated("use andThen", since = "3.0.0-M1")
  def ^<-?[B, C, D](other: PPrism[A, B, C, D]): ApplyFold[S, C] = andThen(other)

  /** alias to composeLens */
  @deprecated("use andThen", since = "3.0.0-M1")
  def ^|->[B, C, D](other: PLens[A, B, C, D]): ApplyFold[S, C] = andThen(other)

  /** alias to composeIso */
  @deprecated("use andThen", since = "3.0.0-M1")
  def ^<->[B, C, D](other: PIso[A, B, C, D]): ApplyFold[S, C] = andThen(other)
}
