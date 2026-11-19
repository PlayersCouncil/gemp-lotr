package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_036_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. The Shadow and
	// regroup phases of turn 1 are at The Troll Hoard (site 2), so a site 4 minion roams there: +2 to play.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bolg", "32_36");

					put("veteran", "32_43");    // Orkish Veteran: [sauron] Orc, strength 7, cost 2, site 4; its regroup ability not used
					put("malevolent", "33_47"); // Malevolent Orc: [sauron] Orc, cost 2; kept in hand
					put("azog", "32_28");       // Azog, Commander of the Wolves Army: [moria] Orc, strength 9; a maneuver ability at a battleground
					put("troop", "33_41");      // Goblin Troop: [moria] Orc, cost 4, site 4; an archer, only ever played in a regroup phase
					put("sauron", "32_45");     // Sauron, The Necromancer: [sauron] Maia, no Orc, strength 13, cost 5; texts on [gundabad] Orcs
					put("bats", "32_30");       // Dark Bats: [moria] Creature, no Orc, cost 1, site 4; a Shadow ability not used
					put("assaulting", "32_27"); // Assaulting Goblin: [moria] Orc; only in the Free Peoples player's discard pile

					put("sceptre", "33_43");    // Great Goblin's Sceptre: a possession that is no mount, borne by a unique Orc
					put("vwarg", "32_32");      // Vicious Warg: a mount borne by an Orc; its play trigger is never met (cheated on)
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void BolgStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Bolg, Captain of the Orc Army
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Damage +1</b>.
		 * Each other Orc is strength +1.
		 * <b>Regroup:</b> If Bolg is not mounted, exert him to play an Orc from your discard pile. Its twilight cost is -2.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("bolg");

		assertEquals("Bolg", card.getBlueprint().getTitle());
		assertEquals("Captain of the Orc Army", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void EachOtherOrcIsStrengthPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bolg = scn.GetShadowCard("bolg");
		var veteran = scn.GetShadowCard("veteran");
		var azog = scn.GetShadowCard("azog");
		var sauron = scn.GetShadowCard("sauron");
		var bilbo = scn.GetRingBearer();

		scn.StartGame();
		scn.MoveMinionsToTable(veteran, azog, sauron);
		assertEquals(7, scn.GetStrength(veteran));
		assertEquals(9, scn.GetStrength(azog));
		assertEquals(13, scn.GetStrength(sauron));
		assertEquals(3, scn.GetStrength(bilbo));

		scn.MoveMinionsToTable(bolg);
		// Orcs of two cultures; not Bolg himself, not a [sauron] minion who is no Orc, not a companion.
		assertEquals(8, scn.GetStrength(veteran));
		assertEquals(10, scn.GetStrength(azog));
		assertEquals(9, scn.GetStrength(bolg));
		assertEquals(13, scn.GetStrength(sauron));
		assertEquals(3, scn.GetStrength(bilbo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void RegroupActionExertsBolgToPlayAnOrcFromTheDiscardPileAtTwoLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bolg = scn.GetShadowCard("bolg");
		var sceptre = scn.GetShadowCard("sceptre");
		var veteran = scn.GetShadowCard("veteran");
		var malevolent = scn.GetShadowCard("malevolent");
		var troop = scn.GetShadowCard("troop");
		var sauron = scn.GetShadowCard("sauron");
		var freepsAssaulting = scn.GetFreepsCard("assaulting");

		scn.StartGame();
		scn.MoveCardsToDiscard(troop, veteran, sauron, freepsAssaulting);
		scn.MoveCardsToHand(bolg, malevolent);
		assertInZone(Zone.DISCARD, troop, veteran, sauron, freepsAssaulting);
		assertInHand(bolg, malevolent);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// Bolg's own play from hand, an Orc played by no ability of his, is at full cost: 4 + 2 roaming.
		int pool = scn.GetTwilight();
		scn.ShadowPlayCard(bolg);
		assertInZone(Zone.SHADOW_CHARACTERS, bolg);
		assertEquals(pool - 6, scn.GetTwilight());
		// A possession that is no mount: Bolg is still not mounted.
		scn.AttachCardsTo(bolg, sceptre);
		assertAttachedTo(sceptre, bolg);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertEquals(0, scn.GetWoundsOn(bolg));
		assertEquals(2, scn.GetTwilightCost(malevolent));
		pool = scn.GetTwilight();
		assertTrue(scn.ShadowActionAvailable(bolg));
		scn.ShadowUseCardAction(bolg);
		assertEquals(1, scn.GetWoundsOn(bolg));
		// Orcs of two cultures; not a [sauron] minion who is no Orc, though it could be paid for, nor the Free
		// Peoples player's Orc in his own discard pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, veteran));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sauron, freepsAssaulting));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(troop);
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		// 4 - 2 + 2 roaming
		assertEquals(pool - 4, scn.GetTwilight());
		assertInZone(Zone.DISCARD, veteran, sauron);
		// The discount is the played Orc's alone.
		assertEquals(2, scn.GetTwilightCost(malevolent));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupActionIsNotOfferedWhileBolgIsMounted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bolg = scn.GetShadowCard("bolg");
		var vwarg = scn.GetShadowCard("vwarg");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveCardsToDiscard(troop);
		assertInZone(Zone.DISCARD, troop);
		scn.SetTwilight(20);
		scn.FreepsPass();
		scn.MoveMinionsToTable(bolg);
		scn.AttachCardsTo(bolg, vwarg);
		assertAttachedTo(vwarg, bolg);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(bolg));
		assertEquals(0, scn.GetWoundsOn(bolg));
		assertInZone(Zone.DISCARD, troop);
	}

	@Test
	public void RegroupActionIsNotOfferedWhileBolgIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bolg = scn.GetShadowCard("bolg");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveCardsToDiscard(troop);
		assertInZone(Zone.DISCARD, troop);
		scn.SetTwilight(20);
		scn.FreepsPass();
		scn.MoveMinionsToTable(bolg);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(bolg, 2);
		assertEquals(2, scn.GetWoundsOn(bolg));

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(bolg));
		assertEquals(2, scn.GetWoundsOn(bolg));
		assertInZone(Zone.DISCARD, troop);
	}

	@Test
	public void RegroupActionWaitsUntilTheOrcCanBePaidForAtTwoLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bolg = scn.GetShadowCard("bolg");
		var troop = scn.GetShadowCard("troop");
		var bats = scn.GetShadowCard("bats");

		scn.StartGame();
		scn.MoveCardsToDiscard(troop, bats);
		assertInZone(Zone.DISCARD, troop, bats);
		scn.FreepsPass();
		scn.MoveMinionsToTable(bolg);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(bolg));
		// Goblin Troop costs 4 - 2 + 2 roaming = 4. With 3 in the pool only Dark Bats (1 + 2 roaming), no Orc, could
		// be paid for.
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(bolg));

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(bolg));
		scn.ShadowUseCardAction(bolg);
		assertEquals(1, scn.GetWoundsOn(bolg));
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.DISCARD, bats);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
