package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_045_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("galadriel", "1_45");

					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: [elven] Elf ally, home site 6, vitality 3
					put("orophin", "1_56");     // Orophin, Lorien Bowman: [elven] Elf ally, home site 6, cost 2, vitality 3
					put("orophin2", "1_56");    // a second copy of the unique Orophin
					put("rumil", "1_57");       // Rumil, Elven Protector: [elven] Elf ally, home site 6, vitality 3
					put("silinde", "1_60");     // Silinde, Elf of Mirkwood: [elven] Elf ally, home site 3, vitality 2
					put("aldor", "4_262");      // Aldor, Soldier of Edoras: [rohan] Man ally, homes 3 and 6 of the Tower block, vitality 2; an archery ability only
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: [elven] Elf ally, cost 4; to play, spot Gandalf or an Elf
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf companion, cost 2
					put("lorienelf", "1_53");   // Lorien Elf: [elven] Elf companion, cost 1; to play, spot an Elf
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf companion, cost 2
					put("bow", "1_41");         // Elven Bow: [elven] possession, bearer must be an Elf
					// Shadow condition: the twilight cost of each companion played to a site that is not a sanctuary is +2
					put("misadventure", "1_129");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

    @Test
    public void GaladrielStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Galadriel, Lady of Light
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 3
		 * Type: Ally
		 * Subtype: Elf
		 * Strength: 3
		 * Vitality: 3
		 * Home: 6
		 * Game Text: At the start of each of your turns, heal every ally whose home is site 6.
		 * Fellowship: Exert Galadriel to play an Elf for free.
		*/

        var scn = GetScenario();

        var card = scn.GetFreepsCard("galadriel");

        assertEquals("Galadriel", card.getBlueprint().getTitle());
        assertEquals("Lady of Light", card.getBlueprint().getSubtitle());
        assertTrue(card.getBlueprint().isUnique());
        assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
        assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
        assertEquals(CardType.ALLY, card.getBlueprint().getCardType());
        assertEquals(Race.ELF, card.getBlueprint().getRace());
        assertEquals(3, card.getBlueprint().getTwilightCost());
        assertEquals(3, card.getBlueprint().getStrength());
        assertEquals(3, card.getBlueprint().getVitality());
		assertTrue(card.getBlueprint().hasAllyHome(new AllyHome(SitesBlock.FELLOWSHIP, 6)));
    }

	@Test
	public void IsPlayedFromHandForThreeToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		scn.MoveCardsToHand(galadriel);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(galadriel));

		scn.FreepsPlayCard(galadriel);
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, galadriel);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

    @Test
    public void Site6AlliesAllHeal() throws DecisionResultInvalidException, CardNotFoundException {
        var scn = GetScenario();

        var galadriel = scn.GetFreepsCard("galadriel");
        var celeborn = scn.GetFreepsCard("celeborn");
        var orophin = scn.GetFreepsCard("orophin");
        var rumil = scn.GetFreepsCard("rumil");
        var silinde = scn.GetFreepsCard("silinde");
        var aldor = scn.GetFreepsCard("aldor");
        var arwen = scn.GetFreepsCard("arwen");
        scn.MoveAlliesToTable(galadriel, silinde, celeborn, orophin, rumil, aldor);
        scn.MoveCompanionsToTable(arwen);

        // Four allies whose home is site 6, Galadriel herself among them.
        scn.AddWoundsToChar(galadriel, 2);
        scn.AddWoundsToChar(celeborn, 1);
        scn.AddWoundsToChar(orophin, 2);
        scn.AddWoundsToChar(rumil, 1);
        // An ally whose home is site 3, an ally whose home is site 6 of the Tower block, and an Elf companion.
        scn.AddWoundsToChar(silinde, 1);
        scn.AddWoundsToChar(aldor, 1);
        scn.AddWoundsToChar(arwen, 1);

        assertEquals(2, scn.GetWoundsOn(galadriel));
        assertEquals(1, scn.GetWoundsOn(celeborn));
        assertEquals(2, scn.GetWoundsOn(orophin));
        assertEquals(1, scn.GetWoundsOn(rumil));
        assertEquals(1, scn.GetWoundsOn(silinde));
        assertEquals(1, scn.GetWoundsOn(aldor));
        assertEquals(1, scn.GetWoundsOn(arwen));

        // The first turn starts here.
        scn.StartGame();

        // Healing removes one wound: Galadriel and Orophin each keep one of their two.
        assertEquals(1, scn.GetWoundsOn(galadriel));
        assertEquals(1, scn.GetWoundsOn(orophin));
        assertEquals(0, scn.GetWoundsOn(celeborn));
        assertEquals(0, scn.GetWoundsOn(rumil));
        assertEquals(1, scn.GetWoundsOn(silinde));
        assertEquals(1, scn.GetWoundsOn(aldor));
        assertEquals(1, scn.GetWoundsOn(arwen));
        assertTrue(scn.AwaitingFellowshipPhaseActions());
    }

	@Test
	public void HealsAtTheStartOfItsOwnersTurnsAndNotTheOpponents() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var orophin = scn.GetFreepsCard("orophin");
		scn.MoveAlliesToTable(galadriel, orophin);
		scn.AddWoundsToChar(orophin, 2);

		// The owner's first turn starts.
		scn.StartGame();
		assertEquals(1, scn.GetWoundsOn(orophin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The opponent's turn starts: not one of "your turns".
		scn.SkipToOtherPlayersTurn();
		assertEquals(scn.P2(), scn.GetCurrentPlayer());
		assertEquals(1, scn.GetWoundsOn(orophin));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));

		// The owner's second turn starts: Orophin is healed a second time.
		scn.SkipToOtherPlayersTurn();
		assertEquals(scn.P1(), scn.GetCurrentPlayer());
		assertEquals(0, scn.GetWoundsOn(orophin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

    @Test
    public void FellowshipActionExertsGaladrielToPlayAnElfForFreeUntilSheIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
        var scn = GetScenario();

        var galadriel = scn.GetFreepsCard("galadriel");
        var elrond = scn.GetFreepsCard("elrond");
        var arwen = scn.GetFreepsCard("arwen");
        var lorienelf = scn.GetFreepsCard("lorienelf");
        var orophin = scn.GetFreepsCard("orophin");
        var gimli = scn.GetFreepsCard("gimli");
        var bow = scn.GetFreepsCard("bow");
        var rumil = scn.GetFreepsCard("rumil");
        var celeborn = scn.GetFreepsCard("celeborn");
        scn.MoveAlliesToTable(galadriel);
        // In hand: two Elf allies, two Elf companions, a companion who is no Elf and an [elven] possession.
        scn.MoveCardsToHand(elrond, arwen, lorienelf, orophin, gimli, bow);
        // An Elf in the discard pile; Celeborn, another, stays in the draw deck.
        scn.MoveCardsToDiscard(rumil);

        scn.StartGame();

        assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
        assertTrue(scn.FreepsActionAvailable(galadriel));

        assertEquals(0, scn.GetWoundsOn(galadriel));
        assertEquals(6, scn.GetFreepsHandCount());
        assertEquals(0, scn.GetTwilight());
        assertEquals(4, scn.GetTwilightCost(elrond));
        assertEquals(2, scn.GetTwilightCost(arwen));

        scn.FreepsUseCardAction(galadriel);
        assertEquals(1, scn.GetWoundsOn(galadriel));
        assertTrue(scn.FreepsHasCardChoiceAvailable(elrond, arwen, lorienelf, orophin));
        assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, bow, rumil, celeborn));
        assertEquals(1, scn.FreepsGetChoiceMin());
        assertEquals(1, scn.FreepsGetChoiceMax());

        // Elrond costs 4, and nothing when she plays him.
        scn.FreepsChooseCard(elrond);
        assertInZone(Zone.SUPPORT, elrond);
        assertEquals(5, scn.GetFreepsHandCount());
        assertEquals(0, scn.GetTwilight());
        assertTrue(scn.AwaitingFellowshipPhaseActions());

        // Only the Elf she played was free: Arwen, left in hand, still costs 2 and is played for 2.
        assertEquals(2, scn.GetTwilightCost(arwen));
        scn.FreepsPlayCard(arwen);
        assertInZone(Zone.FREE_CHARACTERS, arwen);
        assertEquals(2, scn.GetTwilight());

        assertTrue(scn.FreepsActionAvailable(galadriel));
        scn.FreepsUseCardAction(galadriel);
        assertEquals(2, scn.GetWoundsOn(galadriel));
        assertTrue(scn.FreepsHasCardChoiceAvailable(lorienelf, orophin));
        assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, bow));

        // An Elf companion, cost 1
        scn.FreepsChooseCard(lorienelf);
        assertInZone(Zone.FREE_CHARACTERS, lorienelf);
        assertEquals(2, scn.GetTwilight());

        // Vitality 3 with 2 wounds: exhausted, so she can no longer exert, though an Elf in hand could be played.
        assertTrue(scn.FreepsPlayAvailable(orophin));
        assertFalse(scn.FreepsActionAvailable(galadriel));
        assertTrue(scn.AwaitingFellowshipPhaseActions());
    }

	@Test
	public void FellowshipActionIsNotOfferedUntilAnElfInHandCanBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var orophin = scn.GetFreepsCard("orophin");
		var orophin2 = scn.GetFreepsCard("orophin2");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		var gimli = scn.GetFreepsCard("gimli");
		var bow = scn.GetFreepsCard("bow");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveAlliesToTable(galadriel, orophin);
		// In hand: a copy of the unique Orophin who is already in play, a companion who is no Elf and an [elven] possession.
		scn.MoveCardsToHand(orophin2, gimli, bow);
		// An Elf who could be played, but from the discard pile.
		scn.MoveCardsToDiscard(arwen);

		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(galadriel));
		assertEquals(0, scn.GetTwilight());
		assertFalse(scn.FreepsPlayAvailable(orophin2));
		assertTrue(scn.FreepsPlayAvailable(gimli));
		assertTrue(scn.FreepsPlayAvailable(bow));
		assertFalse(scn.FreepsActionAvailable(galadriel));

		scn.MoveCardsToHand(lorienelf);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsActionAvailable(galadriel));

		// One Elf can be played, so it is, and for nothing.
		scn.FreepsUseCardAction(galadriel);
		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertInZone(Zone.FREE_CHARACTERS, lorienelf);
		assertInZone(Zone.HAND, orophin2);
		assertInZone(Zone.DISCARD, arwen);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnElfShePlaysCostsNothingThoughAnotherCardMakesItCostMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var galadriel = scn.GetFreepsCard("galadriel");
		var arwen = scn.GetFreepsCard("arwen");
		var lorienelf = scn.GetFreepsCard("lorienelf");
		// Site 1 is no sanctuary, so each companion played there costs 2 more.
		var misadventure = scn.GetShadowCard("misadventure");
		scn.MoveAlliesToTable(galadriel);
		scn.MoveCardsToSupportArea(misadventure);
		scn.MoveCardsToHand(arwen, lorienelf);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(galadriel));
		// 2 for Arwen + 2 for the condition; 1 for the Lorien Elf + 2
		assertEquals(4, scn.GetTwilightCost(arwen));
		assertEquals(3, scn.GetTwilightCost(lorienelf));

		// "The twilight cost of an Elf played using Galadriel's text is zero, and cannot be raised or lowered."
		scn.FreepsUseCardAction(galadriel);
		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, lorienelf));
		scn.FreepsChooseCard(arwen);
		assertInZone(Zone.FREE_CHARACTERS, arwen);
		assertEquals(0, scn.GetTwilight());

		// The Lorien Elf, played the usual way beside the same condition, costs his 1 and its 2.
		scn.FreepsPlayCard(lorienelf);
		assertInZone(Zone.FREE_CHARACTERS, lorienelf);
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
