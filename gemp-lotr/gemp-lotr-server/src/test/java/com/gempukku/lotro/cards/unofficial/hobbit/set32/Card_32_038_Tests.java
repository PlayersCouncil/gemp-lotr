package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_038_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. The event is played
	// in the regroup phase of turn 1 at The Troll Hoard (site 2), where a site 4 or site 5 minion roams (+2). The
	// minions spotted are laid in the Shadow phase and live through an assignment phase in which nobody is assigned.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("attack", "32_38");

					// [sauron] Orcs for the discard pile
					put("veteran", "32_43");    // Orkish Veteran: cost 2, site 4; its regroup ability not used
					put("malevolent", "33_47"); // Malevolent Orc: cost 2, site 5; an end-of-Shadow-phase offer with no event to take
					put("sentinel", "33_49");   // Orc Sentinel: cost 1, site 5; a Shadow ability not used
					put("ambusher", "33_48");   // Orc Ambusher: only in the Free Peoples player's discard pile

					put("sauron", "32_45");     // Sauron, The Necromancer: [sauron] Maia, no Orc; his assignment ability not used
					put("troop", "33_41");      // Goblin Troop: [moria] Orc, an archer; only ever in a discard pile
					put("sneak", "31_32");      // Goblin Sneak: [moria] Orc, cost 1, site 4; only in a discard pile
					put("azog", "32_28");       // Azog, Commander of the Wolves Army: [moria] Orc; a maneuver ability at a battleground

					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion, for a dead pile
					put("gloin", "30_11");      // Glóin, Father of Gimli: [dwarven] companion, for a discard pile
					put("elrond", "30_21");     // Elrond, Elven Lord: [elven] ally, for a dead pile
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void HiddenAttackStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Hidden Attack
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: Spot an Orc to play a [sauron] Orc (or 2 [sauron] Orcs if you spot a character in the dead pile) from your discard pile.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("attack");

		assertEquals("Hidden Attack", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpotsAnOrcToPlayASauronOrcFromTheDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attack = scn.GetShadowCard("attack");
		var azog = scn.GetShadowCard("azog");
		var veteran = scn.GetShadowCard("veteran");
		var malevolent = scn.GetShadowCard("malevolent");
		var sauron = scn.GetShadowCard("sauron");
		var troop = scn.GetShadowCard("troop");
		var freepsAmbusher = scn.GetFreepsCard("ambusher");
		var shadowBalin = scn.GetShadowCard("balin");
		var freepsGloin = scn.GetFreepsCard("gloin");

		scn.StartGame();
		scn.MoveCardsToDiscard(veteran, malevolent, sauron, troop, freepsAmbusher);
		scn.MoveCardsToHand(attack);
		// No character in the Free Peoples player's dead pile: a companion in the Shadow player's own dead pile, and a
		// companion in the Free Peoples player's discard pile.
		scn.MoveCardsToDeadPile(shadowBalin);
		scn.MoveCardsToDiscard(freepsGloin);
		assertInZone(Zone.DISCARD, veteran, malevolent, sauron, troop, freepsAmbusher, freepsGloin);
		assertInZone(Zone.DEAD, shadowBalin);
		assertEquals(0, scn.GetFreepsDeadCount());
		assertInHand(attack);
		scn.SetTwilight(20);
		scn.FreepsPass();
		// A [moria] Orc to spot.
		scn.MoveMinionsToTable(azog);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertTrue(scn.ShadowPlayAvailable(attack));
		scn.ShadowPlayCard(attack);
		// [sauron] Orcs of two titles; not a [sauron] minion who is no Orc, nor a [moria] Orc, though both could be
		// paid for, nor the Free Peoples player's [sauron] Orc in his own discard pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(veteran, malevolent));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sauron, troop, freepsAmbusher));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(veteran);
		assertInZone(Zone.SHADOW_CHARACTERS, veteran);
		// One Orc only: nothing more is asked.
		assertInZone(Zone.DISCARD, attack, malevolent, sauron, troop);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedWithOnlyAMinionWhoIsNoOrcToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attack = scn.GetShadowCard("attack");
		var sauron = scn.GetShadowCard("sauron");
		var veteran = scn.GetShadowCard("veteran");

		scn.StartGame();
		scn.MoveCardsToDiscard(veteran);
		scn.MoveCardsToHand(attack);
		assertInZone(Zone.DISCARD, veteran);
		assertInHand(attack);
		scn.SetTwilight(20);
		scn.FreepsPass();
		// A [sauron] minion who is no Orc.
		scn.MoveMinionsToTable(sauron);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(attack));
		assertInZone(Zone.DISCARD, veteran);
	}

	@Test
	public void CannotBePlayedUntilASauronOrcInTheDiscardPileCanBePaidFor() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attack = scn.GetShadowCard("attack");
		var malevolent = scn.GetShadowCard("malevolent");
		var veteran = scn.GetShadowCard("veteran");
		var sneak = scn.GetShadowCard("sneak");

		scn.StartGame();
		scn.MoveCardsToDiscard(veteran, sneak);
		scn.MoveCardsToHand(attack);
		assertInZone(Zone.DISCARD, veteran, sneak);
		assertInHand(attack);
		scn.FreepsPass();
		// A [sauron] Orc to spot.
		scn.MoveMinionsToTable(malevolent);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		// Orkish Veteran costs 2 + 2 roaming = 4. With 3 in the pool only Goblin Sneak (1 + 2 roaming), a [moria]
		// Orc, could be paid for.
		scn.SetTwilight(3);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(attack));

		scn.SetTwilight(4);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(attack));
		scn.ShadowPlayCard(attack);
		assertInZone(Zone.SHADOW_CHARACTERS, veteran);
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.DISCARD, attack, sneak);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void PlaysTwoSauronOrcsWhileACompanionIsInTheDeadPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attack = scn.GetShadowCard("attack");
		var azog = scn.GetShadowCard("azog");
		var veteran = scn.GetShadowCard("veteran");
		var malevolent = scn.GetShadowCard("malevolent");
		var sentinel = scn.GetShadowCard("sentinel");
		var sauron = scn.GetShadowCard("sauron");
		var troop = scn.GetShadowCard("troop");
		var balin = scn.GetFreepsCard("balin");

		scn.StartGame();
		scn.MoveCardsToDiscard(veteran, malevolent, sentinel, sauron, troop);
		scn.MoveCardsToHand(attack);
		scn.MoveCardsToDeadPile(balin);
		assertInZone(Zone.DISCARD, veteran, malevolent, sentinel, sauron, troop);
		assertInZone(Zone.DEAD, balin);
		assertEquals(1, scn.GetFreepsDeadCount());
		assertInHand(attack);
		scn.SetTwilight(20);
		scn.FreepsPass();
		scn.MoveMinionsToTable(azog);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		scn.ShadowPlayCard(attack);
		assertTrue(scn.ShadowHasCardChoiceAvailable(veteran, malevolent, sentinel));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sauron, troop));
		scn.ShadowChooseCard(veteran);
		assertInZone(Zone.SHADOW_CHARACTERS, veteran);

		// The second [sauron] Orc, which must be played.
		assertTrue(scn.ShadowHasCardChoiceAvailable(malevolent, sentinel));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sauron, troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(malevolent);
		assertInZone(Zone.SHADOW_CHARACTERS, veteran, malevolent);
		assertInZone(Zone.DISCARD, attack, sentinel, sauron, troop);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void PlaysTwoSauronOrcsWhileAnAllyIsInTheDeadPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attack = scn.GetShadowCard("attack");
		var azog = scn.GetShadowCard("azog");
		var veteran = scn.GetShadowCard("veteran");
		var malevolent = scn.GetShadowCard("malevolent");
		var elrond = scn.GetFreepsCard("elrond");

		scn.StartGame();
		scn.MoveCardsToDiscard(veteran, malevolent);
		scn.MoveCardsToHand(attack);
		scn.MoveCardsToDeadPile(elrond);
		assertInZone(Zone.DISCARD, veteran, malevolent);
		assertInZone(Zone.DEAD, elrond);
		assertEquals(1, scn.GetFreepsDeadCount());
		assertInHand(attack);
		scn.SetTwilight(20);
		scn.FreepsPass();
		scn.MoveMinionsToTable(azog);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		scn.ShadowPlayCard(attack);
		scn.ShadowChooseCard(veteran);
		// The other one, the only candidate left, is played unasked.
		assertInZone(Zone.SHADOW_CHARACTERS, veteran, malevolent);
		assertInZone(Zone.DISCARD, attack);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void PlaysTheOneSauronOrcThereIsWhileACompanionIsInTheDeadPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var attack = scn.GetShadowCard("attack");
		var azog = scn.GetShadowCard("azog");
		var veteran = scn.GetShadowCard("veteran");
		var troop = scn.GetShadowCard("troop");
		var balin = scn.GetFreepsCard("balin");

		scn.StartGame();
		scn.MoveCardsToDiscard(veteran, troop);
		scn.MoveCardsToHand(attack);
		scn.MoveCardsToDeadPile(balin);
		assertInZone(Zone.DISCARD, veteran, troop);
		assertEquals(1, scn.GetFreepsDeadCount());
		assertInHand(attack);
		scn.SetTwilight(20);
		scn.FreepsPass();
		scn.MoveMinionsToTable(azog);

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		// Two cannot be played; the event is still played, for the one.
		assertTrue(scn.ShadowPlayAvailable(attack));
		scn.ShadowPlayCard(attack);
		assertInZone(Zone.SHADOW_CHARACTERS, veteran);
		assertInZone(Zone.DISCARD, attack, troop);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
