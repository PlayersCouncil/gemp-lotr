package com.gempukku.lotro.cards.official.set09;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_09_012_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aiglos", "9_12");
					// Gil-galad, Elven High King: Elf companion, strength 9; to play, spot 2 [elven] companions (cheated
					// in); a regroup ability used only on purpose.
					put("gilgalad", "9_15");
					put("lorienelf", "1_53");   // Lórien Elf: Elf companion, strength 4, no text once in play
					put("orophin", "1_56");     // Orophin, Lórien Bowman: Elf ally (home 6F); an archery ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man companion; a maneuver ability used only on purpose

					// [elven] artifacts.  None of their texts acts in a fellowship phase on these boards.
					put("vilya", "9_23");       // Vilya, Ring of Air: a Ring, cost 0, on Elrond or Gil-galad; a skirmish ability
					put("library", "101_11");   // Library of Rivendell: support area; abilities used only on purpose
					put("ewer", "18_11");       // Galadriel's Silver Ewer: support area; its bonuses are for Galadriel as Ring-bearer
					put("mirror", "15_22");     // The Mirror of Galadriel, Dangerous Guide: support area; answers a draw
					put("gift", "15_16");       // Gift of the Evenstar, Blessed Light: support area; answers a won skirmish or a maneuver phase
					put("phial", "10_13");      // Phial of Galadriel, Star-glass: on a Hobbit; a skirmish ability

					put("bow", "1_41");         // Elven Bow: an [elven] possession (ranged weapon) on an Elf, no strength bonus
					// Narsil, Blade of the Faithful: a [gondor] artifact (hand weapon) on a [gondor] Man; its bonus is
					// for Elendil or Isildur, neither of whom is here.
					put("narsil", "9_34");

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, vitality 3, no text
					put("guard", "10_81");      // Cirith Ungol Guard: [sauron] Uruk-hai, vitality 2; Damage +1 is its whole text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AiglosStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Aiglos
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Artifact
		 * Subtype: Hand Weapon
		 * Vitality: +1
		 * Game Text: Bearer must be an Elf.
		 * If bearer is Gil-galad, he is strength +1 for each [elven] artifact you can spot (limit +6).
		 * <b>Regroup:</b> Discard Aiglos to wound a minion for each Elf you spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("aiglos");

		assertEquals("Aiglos", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void PlaysOnAnyElfAndMakesGilGaladStrengthPlusOneForEachElvenArtifact() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aiglos = scn.GetFreepsCard("aiglos");
		var vilya = scn.GetFreepsCard("vilya");
		var gilgalad = scn.GetFreepsCard("gilgalad");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var orophin = scn.GetFreepsCard("orophin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gilgalad, lorienelf, aragorn);
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToHand(aiglos, vilya);

		scn.StartGame();

		assertEquals(9, scn.GetStrength(gilgalad));
		assertTrue(scn.FreepsPlayAvailable(aiglos));
		scn.FreepsPlayCard(aiglos);

		// Two Elf companions and an Elf ally are offered; the Man is not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gilgalad, lorienelf, orophin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gilgalad);

		assertAttachedTo(aiglos, gilgalad);
		// Aiglos prints no strength: 9 + 1 for the one [elven] artifact, Aiglos itself.
		assertEquals(10, scn.GetStrength(gilgalad));

		// A second [elven] artifact, played on Gil-galad (the only Elrond or Gil-galad).
		scn.FreepsPlayCard(vilya);
		assertAttachedTo(vilya, gilgalad);
		assertEquals(11, scn.GetStrength(gilgalad));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OnlyElvenArtifactsCountBorneOrInTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aiglos = scn.GetFreepsCard("aiglos");
		var gilgalad = scn.GetFreepsCard("gilgalad");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bow = scn.GetFreepsCard("bow");
		var narsil = scn.GetFreepsCard("narsil");
		var library = scn.GetFreepsCard("library");
		scn.MoveCompanionsToTable(gilgalad, lorienelf, aragorn);
		scn.AttachCardsTo(gilgalad, aiglos);
		// An [elven] card that is no artifact, and an artifact of another culture.
		scn.AttachCardsTo(lorienelf, bow);
		scn.AttachCardsTo(aragorn, narsil);

		scn.StartGame();

		// 9 + 1: Aiglos alone.
		assertEquals(10, scn.GetStrength(gilgalad));

		// An [elven] artifact in the support area counts as well as one borne.
		scn.MoveCardsToSupportArea(library);
		assertEquals(11, scn.GetStrength(gilgalad));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BorneByAnotherElfAiglosStrengthensNeitherHimNorGilGalad() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aiglos = scn.GetFreepsCard("aiglos");
		var vilya = scn.GetFreepsCard("vilya");
		var gilgalad = scn.GetFreepsCard("gilgalad");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		scn.MoveCompanionsToTable(gilgalad, lorienelf);
		scn.AttachCardsTo(lorienelf, aiglos);
		scn.AttachCardsTo(gilgalad, vilya);

		scn.StartGame();

		// Two [elven] artifacts in play: Gil-galad bears one, but not Aiglos; the bearer of Aiglos is no Gil-galad.
		assertAttachedTo(aiglos, lorienelf);
		assertEquals(9, scn.GetStrength(gilgalad));
		assertEquals(4, scn.GetStrength(lorienelf));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheBonusStopsAtPlusSix() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aiglos = scn.GetFreepsCard("aiglos");
		var gilgalad = scn.GetFreepsCard("gilgalad");
		var gift = scn.GetFreepsCard("gift");
		scn.MoveCompanionsToTable(gilgalad);
		scn.AttachCardsTo(gilgalad, aiglos, scn.GetFreepsCard("vilya"));
		scn.AttachCardsTo(scn.GetRingBearer(), scn.GetFreepsCard("phial"));
		scn.MoveCardsToSupportArea(scn.GetFreepsCard("library"), scn.GetFreepsCard("ewer"), scn.GetFreepsCard("mirror"));

		scn.StartGame();

		// Six [elven] artifacts: 9 + 6.
		assertEquals(15, scn.GetStrength(gilgalad));

		// A seventh: still 9 + 6.
		scn.MoveCardsToSupportArea(gift);
		assertEquals(15, scn.GetStrength(gilgalad));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RegroupActionDiscardsAiglosToWoundAMinionOnceForEachElfSpottedTheSameMinionMoreThanOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aiglos = scn.GetFreepsCard("aiglos");
		var gilgalad = scn.GetFreepsCard("gilgalad");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var orophin = scn.GetFreepsCard("orophin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCompanionsToTable(gilgalad, lorienelf, aragorn);
		scn.MoveAlliesToTable(orophin);
		scn.AttachCardsTo(gilgalad, aiglos);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		// Minions that came through the skirmishes.
		scn.MoveMinionsToTable(troop, guard);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertAttachedTo(aiglos, gilgalad);
		assertTrue(scn.FreepsActionAvailable(aiglos));
		scn.FreepsUseCardAction(aiglos);
		assertInDiscard(aiglos);

		// Three Elves to spot: two companions and an ally.  The Man is not one of them.
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("2");

		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, guard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gilgalad, lorienelf, aragorn, orophin));
		scn.FreepsChooseCard(troop);
		assertEquals(1, scn.GetWoundsOn(troop));

		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, guard));
		scn.FreepsChooseCard(troop);

		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithOneElfToSpotOneMinionIsWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aiglos = scn.GetFreepsCard("aiglos");
		var gilgalad = scn.GetFreepsCard("gilgalad");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCompanionsToTable(gilgalad, aragorn);
		scn.AttachCardsTo(gilgalad, aiglos);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(troop, guard);

		assertAttachedTo(aiglos, gilgalad);
		scn.FreepsUseCardAction(aiglos);
		assertInDiscard(aiglos);

		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChoose("1");

		scn.FreepsChooseCard(guard);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void TheActionIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aiglos = scn.GetFreepsCard("aiglos");
		var gilgalad = scn.GetFreepsCard("gilgalad");
		scn.MoveCompanionsToTable(gilgalad);
		scn.AttachCardsTo(gilgalad, aiglos);

		scn.StartGame();
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(aiglos));
	}
}
