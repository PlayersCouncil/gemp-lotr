package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_059_Tests
{
	// The Ring-bearer is Frodo, Son of Drogo: a [shire] companion, Ring-bound, vitality 4.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor], strength 8, vitality 4
					// Boromir, Defender of Minas Tirith: [gondor], strength 7, vitality 3.
					// "Fellowship: Add (2) to heal Boromir."
					put("boromir", "3_122");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven], strength 6, vitality 3
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven], strength 6, vitality 3
					// Sam, Son of Hamfast: [shire], Ring-bound and not the Ring-bearer.
					// "Fellowship: Exert Sam to remove a burden."
					put("sam", "1_311");
					// Merry, Friend to Sam: [shire] like Sam and Frodo, and unbound.  Strength 3, vitality 4; a
					// skirmish ability only.
					put("merry", "1_302");
					put("grimir", "1_17");      // Grimir, Dwarven Elder: [dwarven] ally, strength 3, vitality 3

					// Uruk-hai Raiding Party: strength 9, vitality 3, damage +1 and nothing else
					put("raiders", "1_158");
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");
					put("site2", "1_331");
					put("site3", "101_59");
					put("site4", "1_343");
					put("site5", "1_349");
					put("site6", "1_351");
					put("site7", "1_353");
					put("site8", "1_356");
					put("site9", "1_360");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RivendellGatewayStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Rivendell Gateway
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 3
		 * Shadow Number: 0
		 * Direction: Right
		 * Game Text: Sanctuary. Fellowship: Exert an unbound companion to make one of another culture strength +1
		 * 	until the end of the turn (limit +6).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(3);

		assertEquals("Rivendell Gateway", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(3, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.SANCTUARY));
		// The site as first released was a forest as well; this version is not.
		assertFalse(scn.HasKeyword(card, Keyword.FOREST));
	}

	@Test
	public void FellowshipActionExertsAnUnboundCompanionToMakeOneOfAnotherCultureStrengthPlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var arwen = scn.GetFreepsCard("arwen");
		var gimli = scn.GetFreepsCard("gimli");
		var merry = scn.GetFreepsCard("merry");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCompanionsToTable(sam, aragorn, boromir, arwen, gimli, merry);
		scn.MoveAlliesToTable(grimir);

		scn.StartGame();
		scn.SkipToSite(3);

		var gateway = scn.GetCurrentSite();
		assertEquals("Rivendell Gateway", gateway.getBlueprint().getTitle());
		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertTrue(scn.FreepsActionAvailable(gateway));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(6, scn.GetStrength(gimli));

		scn.FreepsUseCardAction(gateway);
		// Who may exert: the unbound companions, Merry among them.  Frodo and Sam, [shire] Hobbits as he is, are
		// Ring-bound and Grimir is an ally; each of them could exert.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, boromir, arwen, gimli, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, sam, grimir));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// Who may be strengthened once a [gondor] companion has exerted: the unbound companions who are not [gondor].
		// Merry is [shire] and unbound; Sam and Frodo are [shire] and Ring-bound; Grimir is a [dwarven] ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, gimli, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, boromir, frodo, sam, grimir));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(arwen);

		assertEquals(7, scn.GetStrength(arwen));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// "Another culture" is another than the culture of whoever exerted: when the [elven] companion exerts, both
		// [gondor] companions may be chosen and she may not.
		scn.FreepsUseCardAction(gateway);
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, boromir, arwen, gimli, merry));
		scn.FreepsChooseCard(arwen);
		assertEquals(1, scn.GetWoundsOn(arwen));

		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, boromir, gimli, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(arwen, frodo, sam, grimir));
		scn.FreepsChooseCard(boromir);

		assertEquals(8, scn.GetStrength(boromir));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(arwen));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void StrengthBonusLastsThroughTheSkirmishesAndBothMovesOfThatTurnAndIsGoneInTheNext() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var raiders = scn.GetShadowCard("raiders");

		scn.StartGame();
		scn.SkipToSite(3);

		var gateway = scn.GetCurrentSite();
		assertEquals("Rivendell Gateway", gateway.getBlueprint().getTitle());
		assertEquals(8, scn.GetStrength(aragorn));

		// Gimli exerts twice.  Aragorn is the only unbound companion of another culture, so he is not asked for.
		scn.FreepsUseCardAction(gateway);
		scn.FreepsChooseCard(gimli);
		assertEquals(9, scn.GetStrength(aragorn));
		scn.FreepsUseCardAction(gateway);
		scn.FreepsChooseCard(gimli);
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(2, scn.GetWoundsOn(gimli));

		// The Raiding Party is one wound from death: the winner of the skirmish is read on it and on Aragorn.
		scn.MoveMinionsToTable(raiders);
		scn.AddWoundsToChar(raiders, 2);

		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());

		// At site 4, in a skirmish: 8 + 2 against 9.  At 8, or at 9, Aragorn would lose and take two wounds.
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, raiders);
		assertEquals(Phase.SKIRMISH, scn.GetCurrentPhase());
		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(9, scn.GetStrength(raiders));
		scn.PassSkirmishActions();
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInDiscard(raiders);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(10, scn.GetStrength(aragorn));

		// A second move, and the last phase of the turn at site 5.
		scn.PassRegroupActions();
		scn.ShadowDeclineReconciliation();
		scn.FreepsChooseToMove();
		assertEquals(5, scn.GetCurrentSiteNumber());
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(scn.P1(), scn.GetCurrentPlayer());
		assertEquals(10, scn.GetStrength(aragorn));

		// The other player's turn, and then the next turn of the player who used it.
		scn.SkipToOtherPlayersTurn();
		assertEquals(scn.P2(), scn.GetCurrentPlayer());
		assertEquals(8, scn.GetStrength(aragorn));

		scn.SkipToOtherPlayersTurn();
		assertEquals(scn.P1(), scn.GetCurrentPlayer());
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void GivesSixInOneTurnHoweverItIsSharedOutAndIsOfferedAgainInTheOtherPlayersTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var arwen = scn.GetFreepsCard("arwen");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, boromir, arwen, gimli);

		// The other player's fellowship, which is at site 3 when its own turn comes.
		var theirAragorn = scn.GetShadowCard("aragorn");
		var theirGimli = scn.GetShadowCard("gimli");
		scn.MoveCompanionsToTable(theirAragorn, theirGimli);

		scn.StartGame();
		scn.SkipToSite(3);

		var gateway = scn.GetCurrentSite();
		assertEquals("Rivendell Gateway", gateway.getBlueprint().getTitle());
		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(6, scn.GetStrength(gimli));

		// Aragorn exerts three times for Arwen: +3.
		for (int i = 1; i <= 3; i++) {
			assertTrue(scn.FreepsActionAvailable(gateway));
			scn.FreepsUseCardAction(gateway);
			scn.FreepsChooseCard(aragorn);
			scn.FreepsChooseCard(arwen);
			assertEquals(6 + i, scn.GetStrength(arwen));
		}
		assertEquals(3, scn.GetWoundsOn(aragorn));

		// Boromir exerts twice for Gimli: +2, and +5 in all.  Aragorn is exhausted and is no longer offered.
		for (int i = 1; i <= 2; i++) {
			assertTrue(scn.FreepsActionAvailable(gateway));
			scn.FreepsUseCardAction(gateway);
			assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, arwen, gimli));
			assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
			scn.FreepsChooseCard(boromir);
			scn.FreepsChooseCard(gimli);
			assertEquals(6 + i, scn.GetStrength(gimli));
		}
		assertEquals(2, scn.GetWoundsOn(boromir));

		// Gimli exerts for Arwen: the sixth.
		assertTrue(scn.FreepsActionAvailable(gateway));
		scn.FreepsUseCardAction(gateway);
		scn.FreepsChooseCard(gimli);
		scn.FreepsChooseCard(arwen);

		// 6 + 4 and 6 + 2: the limit is on what the site gives, not on what one companion gets.
		assertEquals(10, scn.GetStrength(arwen));
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(arwen));

		// Arwen and Gimli could both still exert, for each other or for Aragorn and Boromir.
		assertFalse(scn.FreepsActionAvailable(gateway));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// The other player's fellowship phase at the same site: the limit is new.
		scn.SkipToOtherPlayersTurn();
		assertEquals(scn.P2(), scn.GetCurrentPlayer());
		assertEquals(gateway, scn.GetCurrentSite());
		assertEquals(8, scn.GetStrength(theirAragorn));
		assertTrue(scn.ShadowActionAvailable(gateway));

		scn.ShadowUseCardAction(gateway);
		scn.ShadowChooseCard(theirGimli);
		assertEquals(1, scn.GetWoundsOn(theirGimli));
		assertEquals(9, scn.GetStrength(theirAragorn));
		assertTrue(scn.ShadowActionAvailable(gateway));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void IsNotOfferedUntilAnUnboundCompanionCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCompanionsToTable(sam, boromir, gimli);
		scn.MoveAlliesToTable(grimir);

		scn.StartGame();
		scn.SkipToSite(3);

		var gateway = scn.GetCurrentSite();
		assertEquals("Rivendell Gateway", gateway.getBlueprint().getTitle());

		// Both unbound companions are exhausted (vitality 3).
		scn.AddWoundsToChar(boromir, 2);
		scn.AddWoundsToChar(gimli, 2);

		// Sam's own ability, so that the game asks again with the wounds in place.
		scn.FreepsUseCardAction(sam);
		assertEquals(1, scn.GetWoundsOn(sam));

		// Frodo, Sam (vitality 4) and Grimir (an ally, vitality 3) could each exert.
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(gateway));

		// Boromir heals himself, and can exert again.
		assertEquals(0, scn.GetTwilight());
		scn.FreepsUseCardAction(boromir);
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertTrue(scn.FreepsActionAvailable(gateway));

		// Boromir is the only one who can exert and Gimli the only unbound companion of another culture.
		assertEquals(6, scn.GetStrength(gimli));
		scn.FreepsUseCardAction(gateway);
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(7, scn.GetStrength(gimli));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(2, scn.GetWoundsOn(gimli));
		// The site's action costs no twilight: the pool holds only the (2) of Boromir's ability.
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(gateway));
	}
}
