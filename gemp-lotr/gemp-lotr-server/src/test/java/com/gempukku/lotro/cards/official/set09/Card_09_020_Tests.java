package com.gempukku.lotro.cards.official.set09;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_09_020_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nenya", "9_20");
					put("galadriel", "9_14");   // Galadriel, Bearer of Wisdom: the companion Galadriel; a resistance bonus while she bears an artifact
					put("lady", "1_45");        // Galadriel, Lady of Light: the ally Galadriel, home 6F; heals allies of home 6 at the start of turn, a fellowship ability used only on purpose
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an [elven] Elf companion who is no Galadriel; strength +3 only against a Nazgul
					put("orophin", "1_56");     // Orophin, Lorien Bowman: an [elven] Elf ally who is no Galadriel; an archery ability used only on purpose
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: only a card to hold in hand
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text
					put("filler1", "1_177");    // two more cards that stay in the draw deck, so that "on top" can be told from anywhere else in it
					put("filler2", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NenyaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Nenya, Ring of Adamant
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 0
		 * Type: Artifact
		 * Subtype: Ring
		 * Vitality: +1
		 * Game Text: Bearer must be Galadriel.
		 * 	Maneuver: Heal 2 companions and place up to 2 cards from your hand on top of your draw deck. Discard this artifact.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("nenya");

		assertEquals("Nenya", card.getBlueprint().getTitle());
		assertEquals("Ring of Adamant", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.RING));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getVitality());
	}

	@Test
	public void CannotBePlayedWithoutGaladrielAndIsPlayedOnTheCompanionGaladrielOnceSheIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nenya = scn.GetFreepsCard("nenya");
		var galadriel = scn.GetFreepsCard("galadriel");
		var arwen = scn.GetFreepsCard("arwen");
		var orophin = scn.GetFreepsCard("orophin");
		// An [elven] Elf companion and an [elven] Elf ally, neither of them Galadriel.
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(orophin);
		scn.MoveCardsToHand(nenya, galadriel);

		scn.StartGame();
		assertFalse(scn.FreepsPlayAvailable(nenya));

		scn.FreepsPlayCard(galadriel);
		assertTrue(scn.FreepsPlayAvailable(nenya));

		scn.FreepsPlayCard(nenya);
		assertAttachedTo(nenya, galadriel);
		assertNotAttachedTo(nenya, arwen);
		assertNotAttachedTo(nenya, orophin);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsPlayedOnTheAllyGaladriel() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nenya = scn.GetFreepsCard("nenya");
		var lady = scn.GetFreepsCard("lady");
		var arwen = scn.GetFreepsCard("arwen");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(lady, orophin);
		scn.MoveCardsToHand(nenya);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(nenya));
		scn.FreepsPlayCard(nenya);
		assertAttachedTo(nenya, lady);
		assertNotAttachedTo(nenya, arwen);
		assertNotAttachedTo(nenya, orophin);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ManeuverActionHealsTwoCompanionsPutsUpToTwoCardsOnTheDeckAndDiscardsNenya() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var nenya = scn.GetFreepsCard("nenya");
		var galadriel = scn.GetFreepsCard("galadriel");
		var arwen = scn.GetFreepsCard("arwen");
		var orophin = scn.GetFreepsCard("orophin");
		var gandalf = scn.GetFreepsCard("gandalf");
		var lady = scn.GetFreepsCard("lady");
		var heldTroop = scn.GetFreepsCard("troop");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(galadriel, arwen);
		scn.MoveAlliesToTable(orophin);
		scn.AttachCardsTo(galadriel, nenya);
		// Three cards in hand of different kinds: a companion, an ally and a minion.
		scn.MoveCardsToHand(gandalf, lady, heldTroop);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.AddWoundsToChar(frodo, 2);
		scn.AddWoundsToChar(galadriel, 2);
		scn.AddWoundsToChar(arwen, 2);
		scn.AddWoundsToChar(orophin, 1);
		scn.AddWoundsToChar(troop, 1);

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(2, scn.GetFreepsDeckCount());
		assertEquals(3, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsActionAvailable(nenya));
		scn.FreepsUseCardAction(nenya);

		// Two companions, of any culture, the bearer and the Ring-bearer among them; not the wounded ally or minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, galadriel, arwen));
		assertFalse(scn.FreepsHasCardChoiceAvailable(orophin));
		assertFalse(scn.FreepsHasCardChoiceAvailable(troop));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(galadriel, arwen);

		// Up to 2 cards from a hand of 3.
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(gandalf, lady);
		// The player orders the two he puts back: the first named goes on first, under the second.
		scn.FreepsChooseCard(gandalf);

		// One wound each healed from two; Frodo, the ally and the minion keep theirs.
		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(orophin));
		assertEquals(1, scn.GetWoundsOn(troop));

		// The two chosen are the top two cards of the draw deck; the third stays in hand.
		assertEquals(1, scn.GetFreepsHandCount());
		assertInHand(heldTroop);
		assertEquals(4, scn.GetFreepsDeckCount());
		assertSame(lady, scn.GetFromTopOfFreepsDeck(1));
		assertSame(gandalf, scn.GetFromTopOfFreepsDeck(2));

		assertInDiscard(nenya);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void IsAManeuverActionAndNotOfferedInTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nenya = scn.GetFreepsCard("nenya");
		var galadriel = scn.GetFreepsCard("galadriel");
		var arwen = scn.GetFreepsCard("arwen");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(galadriel, arwen);
		scn.AttachCardsTo(galadriel, nenya);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.AddWoundsToChar(arwen, 1);

		scn.SkipToPhase(Phase.MANEUVER);
		assertTrue(scn.FreepsActionAvailable(nenya));

		scn.SkipToPhase(Phase.ARCHERY);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsActionAvailable(nenya));
		assertAttachedTo(nenya, galadriel);
	}
}
