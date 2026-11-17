package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_080_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gothmog", "18_80");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Gothmog, and a Mountain Orc in the discard pile.
	protected VirtualTableScenario GetGothmogScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gothmog", "18_80");
					// Mountain Orc: [orc] Orc, cost 3, site number 4, vitality 2; its text answers an assignment
					put("orc", "11_129");
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
					put("gothmog", "18_80");
					put("legolas", "1_50");         // Legolas, Greenleaf: archer; an archery ability used only on purpose
					put("marksman", "1_176");       // Goblin Marksman: archer, and no other text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetChoiceScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gothmog", "18_80");
					// Orc Miscreant: [orc] Orc, cost 3, site 4, vitality 3; to play, spot an [orc] minion; a regroup
					// ability used only on purpose
					put("miscreant", "11_131");
					// Half-troll of Far Harad: [orc] minion who is no Orc, cost 3, site 4; a start-of-assignment trigger
					put("halftroll", "14_12");
					// The Beckoning Shadow: [orc] condition, cost 0 (to play, exert an [orc] minion)
					put("beckoning", "12_83");
					put("soldier", "1_271");        // Orc Soldier: [sauron] Orc, cost 2, site 6; a skirmish ability
					// Scurrying Goblin: [orc] Orc, cost 1; adds (2) only at an underground site
					put("goblin", "11_137");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetNerteaScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gothmog", "18_80");
					put("miscreant", "11_131");
					// Isengard Informant: "Regroup: Exert this minion to play an [orc] Troll from your discard pile."
					put("informant", "13_110");
					// Beastly Olog-hai: [orc] Troll, cost 6, site 5; to play, spot an [orc] minion
					put("olog", "11_108");
					// Orc Spear: [orc] possession, cost 1, bearer must be an [orc] minion
					put("spear", "12_96");
					// Úlairë Nertëa, Sanctified for Cruelty: "Each time you play a minion, you may exert it to play a
					// possession on it from your discard pile"
					put("nertea", "103_77");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GothmogStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Gothmog, Morgul Leader
		 * Unique: True
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 15
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: Each of the fellowship and minion archery totals is +3.<br><b>Regroup:</b> Remove (2) to play an [orc] minion from your discard pile. That minion comes into play exhausted.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gothmog");

		assertEquals("Gothmog", card.getBlueprint().getTitle());
		assertEquals("Morgul Leader", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(15, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	// "Remove (2) to play an [orc] minion from your discard pile": a play from a pile as an effect is treated as
	// a cost, so the pool must pay for the removal and the minion together (F-0046, F-0141).  The definition did
	// not count the (2) it removes: with the minion's cost in the pool and no more the ability was offered, took
	// the tokens and played nothing.
	// F-0453.
	@Test
	public void ThePoolMustPayForTheRemovalAndTheOrcMinionTogether() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGothmogScenario();

		var gothmog = scn.GetShadowCard("gothmog");
		var orc = scn.GetShadowCard("orc");
		scn.MoveCardsToDiscard(orc);

		scn.StartGame();
		scn.FreepsPass();
		scn.ShadowPass();
		// Gothmog is put down in the regroup phase: before it his archery bonus would be in the way.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(gothmog);

		// At site 2 the Mountain Orc is roaming: 3 and 2 more.  (6) in the pool pays for him, or for the removal
		// and four tokens beside.
		scn.SetTwilight(6);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(gothmog));

		scn.SetTwilight(7);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(gothmog));
		scn.ShadowUseCardAction(gothmog);

		// "That minion comes into play exhausted."
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, orc);
		assertEquals(1, scn.GetWoundsOn(orc));
		assertTrue(scn.IsExhausted(orc));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheFellowshipAndMinionArcheryTotalsAreEachThreeHigherOnceGothmogIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetArcheryScenario();

		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(legolas);

		var gothmog = scn.GetShadowCard("gothmog");
		var marksman = scn.GetShadowCard("marksman");
		scn.MoveCardsToHand(gothmog);

		scn.StartGame();
		scn.MoveMinionsToTable(marksman);
		// Gothmog is 6, and roaming at site 2.
		scn.SetTwilight(8);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		// One archer on each side.
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertEquals(1, scn.GetShadowArcheryTotal());

		scn.ShadowPlayCard(gothmog);

		assertInZone(Zone.SHADOW_CHARACTERS, gothmog);
		assertEquals(4, scn.GetFreepsArcheryTotal());
		assertEquals(4, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void RegroupAbilityRemoves2ToPlayAnOrcMinionFromYourDiscardPileExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetChoiceScenario();

		var gothmog = scn.GetShadowCard("gothmog");
		var miscreant = scn.GetShadowCard("miscreant");
		var halftroll = scn.GetShadowCard("halftroll");
		var beckoning = scn.GetShadowCard("beckoning");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToDiscard(miscreant, halftroll, beckoning, soldier);
		// The Free Peoples player's own [orc] Orc, in his discard pile: a title the Shadow player's piles do not hold.
		var goblin = scn.GetFreepsCard("goblin");
		scn.MoveCardsToDiscard(goblin);

		scn.StartGame();
		scn.FreepsPass();
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(gothmog);

		// 2 removed + 3 for the Miscreant or the Half-troll + 2 roaming at site 2: exactly enough.
		scn.SetTwilight(7);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(gothmog));
		assertInDiscard(miscreant, halftroll, beckoning, soldier, goblin);

		scn.ShadowUseCardAction(gothmog);
		// The (2) is gone before the minion is chosen.
		assertEquals(5, scn.GetTwilight());
		// An [orc] Orc and an [orc] Half-troll are offered. An [orc] condition, a [sauron] Orc (2 + 2 roaming: the
		// pool would pay for him) and the other player's [orc] Orc in his own pile are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(miscreant, halftroll));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(beckoning, soldier, goblin));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCards(miscreant);

		assertInZone(Zone.SHADOW_CHARACTERS, miscreant);
		// Vitality 3: exhausted is 2 wounds.
		assertEquals(2, scn.GetWoundsOn(miscreant));
		assertTrue(scn.IsExhausted(miscreant));
		// Gothmog, another [orc] minion, is untouched, and the Half-troll is still in the discard pile.
		assertEquals(0, scn.GetWoundsOn(gothmog));
		assertInDiscard(halftroll, beckoning, soldier, goblin);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWithNoOrcMinionInYourDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetChoiceScenario();

		var gothmog = scn.GetShadowCard("gothmog");
		var beckoning = scn.GetShadowCard("beckoning");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToDiscard(beckoning, soldier);
		var goblin = scn.GetFreepsCard("goblin");
		scn.MoveCardsToDiscard(goblin);

		scn.StartGame();
		scn.FreepsPass();
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.MoveMinionsToTable(gothmog);

		// Enough to remove (2) and play any of them; only none is an [orc] minion of his own discard pile.
		scn.SetTwilight(10);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(gothmog));
		assertEquals(10, scn.GetTwilight());
		assertInDiscard(beckoning, soldier, goblin);
	}

	@Test
	public void TheMinionPlayedIsExhaustedAsItComesIntoPlaySoItCannotExertForUlaireNertea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetNerteaScenario();

		var gothmog = scn.GetShadowCard("gothmog");
		var informant = scn.GetShadowCard("informant");
		var nertea = scn.GetShadowCard("nertea");
		var miscreant = scn.GetShadowCard("miscreant");
		var olog = scn.GetShadowCard("olog");
		var spear = scn.GetShadowCard("spear");
		scn.MoveCardsToDiscard(miscreant, olog, spear);

		scn.StartGame();
		scn.FreepsPass();
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.MoveMinionsToTable(gothmog, informant, nertea);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// The control: an [orc] Troll played from the discard pile by Isengard Informant comes into play fresh, and
		// Nertëa offers to exert it to play the Orc Spear on it.
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		scn.ShadowUseCardAction(informant);
		assertInZone(Zone.SHADOW_CHARACTERS, olog);
		assertEquals(0, scn.GetWoundsOn(olog));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(nertea));
		scn.ShadowDeclineOptionalTrigger();
		assertInDiscard(spear);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(gothmog));
		scn.ShadowUseCardAction(gothmog);

		// "Comes into play exhausted": by the time its play is answered the Miscreant cannot exert, so Nertëa
		// has nothing to offer.
		assertInZone(Zone.SHADOW_CHARACTERS, miscreant);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(nertea));
		assertEquals(2, scn.GetWoundsOn(miscreant));
		assertInDiscard(spear);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
