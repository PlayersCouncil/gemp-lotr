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

public class Card_11_089_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bow", "11_89");
					// A second copy, already in play when the first is played.
					put("bow2", "11_89");
					// Poleaxe: another [men] possession, a hand weapon for a [men] minion.  Its skirmish ability is never used.
					put("poleaxe", "11_95");

					// Axeman of Harad: a [men] Man minion.  Its text only lowers its own cost in hand.
					put("axeman", "11_69");
					// Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit, site 2.  His maneuver and regroup
					// abilities are never used.
					put("ted", "18_75");
					// Dunlending Brigand: a [dunland] Man minion with no text.
					put("brigand", "4_10");

					// Aragorn, Ranger of the North: a [gondor] Man companion.  A maneuver ability, never used.
					put("aragorn", "1_89");
					// Boromir, Son of Denethor: a [gondor] Man companion, vitality 3; exhausted where he is laid.
					put("boromir", "1_97");
					// Barliman Butterbur: a [gandalf] Man ally, vitality 2.  His fellowship ability is never used.
					put("barliman", "1_70");
					// Sam, Son of Hamfast: a Hobbit companion.  His abilities are never used.
					put("sam", "1_311");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LongBattleBowStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Long Battle Bow
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Ranged weapon
		 * Strength: 1
		 * Game Text: Bearer must be a [men] minion.
		 * 		Bearer is an <b>archer</b>.
		 * 		When you play this possession, you may make the Free Peoples player exert his or her Man.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("bow");

		assertEquals("Long Battle Bow", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.RANGED_WEAPON));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void PlayedOnAMenMinionItMakesHimAnArcherAndTheFreePeoplesPlayerExertsAManOfHisChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var barliman = scn.GetFreepsCard("barliman");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, boromir, sam);
		scn.MoveAlliesToTable(barliman);
		scn.AddWoundsToChar(boromir, 2);

		var bow = scn.GetShadowCard("bow");
		var axeman = scn.GetShadowCard("axeman");
		var ted = scn.GetShadowCard("ted");
		var brigand = scn.GetShadowCard("brigand");

		scn.StartGame();
		scn.SetShadowHand(bow);
		scn.MoveMinionsToTable(axeman, ted, brigand);
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(axeman));
		scn.FreepsPass();

		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertFalse(scn.HasKeyword(axeman, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(ted, Keyword.ARCHER));

		scn.ShadowPlayCard(bow);

		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(axeman, ted));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(brigand, aragorn));

		scn.ShadowChooseCard(axeman);
		assertAttachedTo(bow, axeman);
		assertTrue(scn.HasKeyword(axeman, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(ted, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(brigand, Keyword.ARCHER));

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(bow));
		scn.ShadowAcceptOptionalTrigger();

		// The Free Peoples player chooses which of his Men exerts: a companion or an ally.  Not a Hobbit, not an
		// exhausted Man, and not the Shadow player's Men.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, barliman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, sam, boromir, axeman, brigand));

		scn.FreepsChooseCard(barliman);
		assertEquals(1, scn.GetWoundsOn(barliman));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(axeman));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningTheOfferExertsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(barliman);

		var bow = scn.GetShadowCard("bow");
		var axeman = scn.GetShadowCard("axeman");

		scn.StartGame();
		scn.SetShadowHand(bow);
		scn.MoveMinionsToTable(axeman);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(barliman));
		scn.FreepsPass();

		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		scn.ShadowPlayCard(bow);
		assertAttachedTo(bow, axeman);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(bow));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertAttachedTo(bow, axeman);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnlyItsOwnPlaySetsItOffAndNotACopyAlreadyInPlayOrAnotherMenPossessionPlayedAfterIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var bow = scn.GetShadowCard("bow");
		var bow2 = scn.GetShadowCard("bow2");
		var poleaxe = scn.GetShadowCard("poleaxe");
		var axeman = scn.GetShadowCard("axeman");
		var ted = scn.GetShadowCard("ted");

		scn.StartGame();
		scn.SetShadowHand(bow, poleaxe);
		scn.MoveMinionsToTable(axeman, ted);
		scn.AttachCardsTo(ted, bow2);
		assertAttachedTo(bow2, ted);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsPass();

		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		scn.ShadowPlayCard(bow);
		assertAttachedTo(bow, axeman);

		// The copy on Ted makes no offer of its own.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(bow));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(bow2));
		scn.ShadowAcceptOptionalTrigger();

		// Aragorn is his only Man, and exerts unasked.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Another [men] possession played after it, with a Man still able to exert: no offer.
		scn.ShadowPlayCard(poleaxe);
		scn.ShadowChooseCard(ted);
		assertAttachedTo(poleaxe, ted);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoManOfTheFreePeoplesPlayerAbleToExertNoOfferIsMade() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(boromir, sam);
		scn.AddWoundsToChar(boromir, 2);

		var bow = scn.GetShadowCard("bow");
		var axeman = scn.GetShadowCard("axeman");

		scn.StartGame();
		scn.SetShadowHand(bow);
		scn.MoveMinionsToTable(axeman);
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(sam));
		scn.FreepsPass();

		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		scn.ShadowPlayCard(bow);

		// Boromir, the one Man, is exhausted; Sam and Frodo are Hobbits.
		assertAttachedTo(bow, axeman);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
