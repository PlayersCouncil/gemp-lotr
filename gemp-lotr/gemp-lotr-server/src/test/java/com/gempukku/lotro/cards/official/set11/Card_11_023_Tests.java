package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_023_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bow", "11_23");
					// Legolas, Companion of the Ring: no archer in print; his own text needs a battleground or a forest
					put("legolas", "11_21");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an Elf companion who is no archer; +3 against a Nazgûl only
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: an Elf ally; a start-of-turn heal and a fellowship ability only
					put("gimli", "1_13");       // Gimli, Son of Glóin: a Dwarf companion; a skirmish ability only
					// Something Draws Near: an [elven] event, Fellowship or Maneuver: "Exert an Elf to discard an
					// [isengard] possession or [isengard] artifact."  No such card is in play: it is played for its cost.
					put("near1", "3_26");
					put("near2", "3_26");
					put("near3", "3_26");
					// Phial of Galadriel: an [elven] possession, "To play, exert an Elf. Bearer must be the Ring-bearer."
					put("phial", "3_24");
					// I Name You Elf-Friend: a [dwarven] Maneuver event, "Exert an Elf to make a Dwarf strength +3 and
					// damage +1 until the regroup phase."
					put("elffriend", "101_4");
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetArcheryScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bow", "11_23");
					// Legolas, Greenleaf: "Archery: Exert Legolas to wound a minion; Legolas does not add to the
					// fellowship archery total."  An exertion of Legolas that plays no card.
					put("legolas", "1_50");
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, vitality 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetToilScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bow", "11_23");
					// Legolas, Companion of the Ring: vitality 3; his own text needs a battleground or a forest
					put("legolas", "11_21");
					// Elven Marksmanship: [elven] condition of the support area, cost 6, not unique.  "To play, spot
					// an Elf.  Toil 2."  Its archery ability is not used.
					put("marksmanship1", "11_17");
					put("marksmanship2", "11_17");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LegolasBowStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Legolas' Bow
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Ranged weapon
		 * Game Text: Bearer must be an Elf.<br>Bearer is an <b>archer</b>.<br>If bearer is Legolas, each time you exert him to play an [elven] condition or [elven] event, you may heal him (limit once per phase).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("bow");

		assertEquals("Legolas' Bow", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.RANGED_WEAPON));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnAnElfAndMakesThatBearerAnArcher() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		var elrond = scn.GetFreepsCard("elrond");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(legolas, arwen, gimli);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToHand(bow);

		scn.StartGame();
		assertFalse(scn.HasKeyword(arwen, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(legolas, Keyword.ARCHER));
		assertTrue(scn.FreepsPlayAvailable(bow));

		scn.FreepsPlayCard(bow);
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, arwen, elrond));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(arwen);

		assertAttachedTo(bow, arwen);
		assertTrue(scn.HasKeyword(arwen, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(legolas, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(elrond, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(gimli, Keyword.ARCHER));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnAllyBearingItIsAnArcher() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var arwen = scn.GetFreepsCard("arwen");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(elrond);
		scn.AttachCardsTo(elrond, bow);

		scn.StartGame();
		assertAttachedTo(bow, elrond);
		assertTrue(scn.HasKeyword(elrond, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(arwen, Keyword.ARCHER));
	}

	@Test
	public void ExertingLegolasToPlayAnElvenEventOffersAHealOfOneWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		var near1 = scn.GetFreepsCard("near1");
		scn.MoveCompanionsToTable(legolas, arwen);
		scn.AttachCardsTo(legolas, bow);
		scn.MoveCardsToHand(near1);

		scn.StartGame();
		// One wound to begin with, so that a heal of one wound reads apart from a heal of them all.
		scn.AddWoundsToChar(legolas, 1);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(arwen));

		scn.FreepsPlayCard(near1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, arwen));
		scn.FreepsChooseCard(legolas);
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bow));

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertInDiscard(near1);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DecliningTheHealLeavesTheExertion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var legolas = scn.GetFreepsCard("legolas");
		var near1 = scn.GetFreepsCard("near1");
		scn.MoveCompanionsToTable(legolas);
		scn.AttachCardsTo(legolas, bow);
		scn.MoveCardsToHand(near1);

		scn.StartGame();
		assertEquals(0, scn.GetWoundsOn(legolas));

		// Legolas is the only Elf: he pays for Something Draws Near unasked.
		scn.FreepsPlayCard(near1);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bow));

		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertInDiscard(near1);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheHealIsOncePerPhaseAndOfferedAgainInTheNextPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var legolas = scn.GetFreepsCard("legolas");
		var near1 = scn.GetFreepsCard("near1");
		var near2 = scn.GetFreepsCard("near2");
		var near3 = scn.GetFreepsCard("near3");
		scn.MoveCompanionsToTable(legolas);
		scn.AttachCardsTo(legolas, bow);
		scn.MoveCardsToHand(near1, near2, near3);

		scn.StartGame();
		assertEquals(0, scn.GetWoundsOn(legolas));

		scn.FreepsPlayCard(near1);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bow));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A second exertion to play an [elven] event in the same fellowship phase: the limit is reached.
		scn.FreepsPlayCard(near2);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(bow));
		assertInDiscard(near2);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		var troop = scn.GetShadowCard("troop");
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(1, scn.GetWoundsOn(legolas));

		scn.FreepsPlayCard(near3);
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bow));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertInDiscard(near3);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NotOfferedWhenAnotherElfIsExertedToPlayAnElvenEvent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		var near1 = scn.GetFreepsCard("near1");
		scn.MoveCompanionsToTable(legolas, arwen);
		scn.AttachCardsTo(legolas, bow);
		scn.MoveCardsToHand(near1);

		scn.StartGame();
		// Wounded, so that a heal of Legolas could do something.
		scn.AddWoundsToChar(legolas, 1);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(arwen));

		scn.FreepsPlayCard(near1);
		scn.FreepsChooseCard(arwen);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(bow));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertInDiscard(near1);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWhenLegolasIsExertedToPlayAnElvenPossessionOrADwarvenEvent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var legolas = scn.GetFreepsCard("legolas");
		var gimli = scn.GetFreepsCard("gimli");
		var phial = scn.GetFreepsCard("phial");
		var elffriend = scn.GetFreepsCard("elffriend");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(legolas, gimli);
		scn.AttachCardsTo(legolas, bow);
		scn.MoveCardsToHand(phial, elffriend);

		scn.StartGame();
		assertEquals(0, scn.GetWoundsOn(legolas));

		// Legolas is the only Elf and Frodo the only Ring-bearer: the Phial goes on Frodo and Legolas pays, unasked.
		scn.FreepsPlayCard(phial);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(bow));
		assertAttachedTo(phial, frodo);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		var troop = scn.GetShadowCard("troop");
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(6, scn.GetStrength(gimli));

		// Gimli is the only Dwarf: he is made strength +3 unasked.
		scn.FreepsPlayCard(elffriend);
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(bow));
		assertEquals(9, scn.GetStrength(gimli));
		assertInDiscard(elffriend);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NotOfferedWhenLegolasIsNotTheBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetFreepsCard("bow");
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		var near1 = scn.GetFreepsCard("near1");
		scn.MoveCompanionsToTable(legolas, arwen);
		scn.AttachCardsTo(arwen, bow);
		scn.MoveCardsToHand(near1);

		scn.StartGame();
		scn.AddWoundsToChar(arwen, 1);
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(arwen));

		scn.FreepsPlayCard(near1);
		scn.FreepsChooseCard(legolas);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(bow));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertInDiscard(near1);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NotOfferedWhenLegolasExertsForAnAbilityThatPlaysNoCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetArcheryScenario();

		var bow = scn.GetFreepsCard("bow");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);
		scn.AttachCardsTo(legolas, bow);

		scn.StartGame();
		var troop = scn.GetShadowCard("troop");
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(troop));

		// The Troop is the only minion: it is wounded unasked.
		scn.FreepsUseCardAction(legolas);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(bow));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void ExertingLegolasForToilToPlayAnElvenConditionOffersTheHeal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetToilScenario();

		var bow = scn.GetFreepsCard("bow");
		var legolas = scn.GetFreepsCard("legolas");
		var marksmanship1 = scn.GetFreepsCard("marksmanship1");
		var marksmanship2 = scn.GetFreepsCard("marksmanship2");
		scn.MoveCompanionsToTable(legolas);
		scn.AttachCardsTo(legolas, bow);
		scn.MoveCardsToHand(marksmanship1, marksmanship2);

		scn.StartGame();
		// One wound to begin with, so that a heal of one wound reads apart from a heal of them all.
		scn.AddWoundsToChar(legolas, 1);

		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(legolas));

		// Played with nobody exerted for toil: the whole cost, and nothing for the Bow to answer.
		scn.FreepsPlayCard(marksmanship1);
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas));
		scn.FreepsDeclineChoosing();
		assertInZone(Zone.SUPPORT, marksmanship1);
		assertEquals(6, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(bow));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Played with Legolas exerted for toil: that exertion is one made to play an [elven] condition, and
		// the Bow answers it while the condition is still on its way to the table.
		scn.FreepsPlayCard(marksmanship2);
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas));
		scn.FreepsChooseCard(legolas);
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bow));

		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertInZone(Zone.SUPPORT, marksmanship2);
		// 6 + (6 - 2)
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
