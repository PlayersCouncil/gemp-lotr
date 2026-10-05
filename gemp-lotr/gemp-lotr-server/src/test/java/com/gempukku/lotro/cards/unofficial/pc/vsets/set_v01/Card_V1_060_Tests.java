package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.SitesBlock;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_060_Tests
{
	// The Ring-bearer is Frodo, Son of Drogo.  He is kept out of every skirmish here: in this format his skirmish
	// may not be canceled whatever the site says.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Three Hobbits of strength 3 and vitality 4, none with text that comes into play here
					put("merry", "1_302");      // Merry, Friend to Sam
					put("pippin", "1_306");     // Pippin, Friend to Frodo
					put("sam", "1_311");        // Sam, Son of Hamfast
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8
					// Hobbit Stealth: "Skirmish: At sites 1 to 5, cancel a skirmish involving a Hobbit."  Costs 1.
					put("stealth1", "1_298");
					put("stealth2", "1_298");
					put("stealth3", "1_298");

					// Cave Troll of Moria, Scourge of the Black Pit: unique [moria] Troll, strength 15, vitality 4,
					// damage +1, fierce.  "To play, spot a [moria] Orc.  At an underground site, Cave Troll of
					// Moria's twilight cost is -3."  Costs 10.
					put("troll", "1_165");
					// Guard Commander: unique [moria] Orc, strength 7, vitality 2; a skirmish ability the Shadow
					// player never uses here
					put("commander", "1_186");
					// Goblin Runner: [moria] Orc, not unique, strength 5, vitality 1
					put("runner", "1_178");
					put("runner2", "1_178");
					put("savage", "1_151");     // Uruk Savage: [isengard], not unique, strength 5, vitality 3
					// Ulaire Lemenya: unique [ringwraith] Nazgul; his text needs a [ringwraith] condition on a companion
					put("lemenya", "1_232");
					// The Long Dark: unique [moria] condition; adds twilight for Dwarf companions, of whom there are none
					put("longDark", "1_188");
				}},
				new HashMap<>()
				{{
					put("site1", "1_319");
					put("site2", "1_331");
					put("site3", "1_341");
					put("site4", "101_60");
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
	public void MoriaCrossroadsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: Moria Crossroads
		 * Unique: False
		 * Type: Site
		 * Block: Fellowship
		 * Site Number: 4
		 * Shadow Number: 3
		 * Direction: Right
		 * Game Text: Underground. While you can spot a unique [moria] minion, skirmishes involving [moria] minions
		 * 	may not be canceled.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite(4);

		assertEquals("Moria Crossroads", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(4, card.getBlueprint().getSiteNumber());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.UNDERGROUND));
	}

	@Test
	public void WithAUniqueMoriaMinionInPlayOnlySkirmishesInvolvingMoriaMinionsAreKeptFromBeingCanceled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		var stealth1 = scn.GetFreepsCard("stealth1");
		var stealth2 = scn.GetFreepsCard("stealth2");
		var stealth3 = scn.GetFreepsCard("stealth3");
		scn.MoveCompanionsToTable(merry, pippin, sam);

		var troll = scn.GetShadowCard("troll");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(3);

		scn.MoveCardsToHand(stealth1, stealth2, stealth3);
		scn.MoveCardsToHand(troll);
		scn.MoveMinionsToTable(runner, savage);
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPass();
		assertEquals("Moria Crossroads", scn.GetCurrentSite().getBlueprint().getTitle());

		// The unique [moria] minion is played from hand, and is a Troll: the site asks for a minion, not an Orc.
		// 4 companions + 3 for the site; the Cave Troll costs 10 - 3 at an underground site, which this site is.
		assertEquals(7, scn.GetTwilight());
		scn.ShadowPlayCard(troll);
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, troll);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{merry, savage},
				new PhysicalCardImpl[]{pippin, runner},
				new PhysicalCardImpl[]{sam, troll}
		);

		// A skirmish involving an [isengard] minion and no [moria] one is canceled as usual.
		scn.FreepsResolveSkirmish(merry);
		assertTrue(scn.IsCharSkirmishingAgainst(merry, savage));
		assertTrue(scn.FreepsPlayAvailable(stealth1));
		scn.FreepsPlayCard(stealth1);
		assertInDiscard(stealth1);
		assertFalse(scn.IsCharSkirmishing(merry));
		assertFalse(scn.IsCharAssigned(merry));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// A skirmish involving a [moria] minion that is not unique, while the unique one waits for its own skirmish:
		// the event is played and paid for, and the skirmish goes on and is lost.  3 against 5.
		scn.FreepsResolveSkirmish(pippin);
		assertEquals(1, scn.GetTwilight());
		scn.FreepsPlayCard(stealth2);
		assertInDiscard(stealth2);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.IsCharSkirmishingAgainst(pippin, runner));
		assertFalse(scn.IsCharSkirmishing(troll));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(pippin));
		scn.ShadowPass();
		scn.FreepsPass();
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(runner));

		// The skirmish of the unique [moria] minion itself, with no Orc in it.  3 against 15: Sam is overwhelmed.
		scn.FreepsResolveSkirmish(sam);
		scn.FreepsPlayCard(stealth3);
		assertInDiscard(stealth3);
		assertTrue(scn.IsCharSkirmishingAgainst(sam, troll));
		assertEquals(15, scn.GetStrength(troll));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.ShadowPass();
		scn.FreepsPass();
		assertInZone(Zone.DEAD, sam);

		// The Cave Troll is fierce and lives, so the skirmishes are followed by a second assignment.
		assertEquals(Phase.ASSIGNMENT, scn.GetCurrentPhase());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void OnceTheUniqueMoriaMinionIsKilledASkirmishInvolvingAMoriaMinionIsCanceledAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var stealth1 = scn.GetFreepsCard("stealth1");
		var stealth2 = scn.GetFreepsCard("stealth2");
		scn.MoveCompanionsToTable(merry, pippin, aragorn);

		var commander = scn.GetShadowCard("commander");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		var lemenya = scn.GetShadowCard("lemenya");
		var longDark = scn.GetShadowCard("longDark");

		scn.StartGame();
		scn.SkipToSite(3);

		scn.MoveCardsToHand(stealth1, stealth2);
		scn.MoveMinionsToTable(commander, runner, runner2, lemenya);
		// The Guard Commander (vitality 2) is one wound from death.
		scn.AddWoundsToChar(commander, 1);

		scn.FreepsPass();
		assertEquals("Moria Crossroads", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.MoveCardsToSupportArea(longDark);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{merry, runner},
				new PhysicalCardImpl[]{aragorn, commander},
				new PhysicalCardImpl[]{pippin, runner2}
		);
		// Ulaire Lemenya is left out of the fighting.
		scn.ShadowDeclineAssignments();

		// With the Guard Commander alive, Merry's skirmish with a Goblin Runner is not canceled.  3 against 5.
		scn.FreepsResolveSkirmish(merry);
		scn.FreepsPlayCard(stealth1);
		assertInDiscard(stealth1);
		assertTrue(scn.IsCharSkirmishingAgainst(merry, runner));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.ShadowPass();
		scn.FreepsPass();
		assertEquals(1, scn.GetWoundsOn(merry));

		// Aragorn kills the Guard Commander.  8 against 7.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInDiscard(commander);
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// Still in play: two [moria] minions that are not unique, a unique minion that is not [moria], and a unique
		// [moria] card that is not a minion.
		assertInZone(Zone.SHADOW_CHARACTERS, runner, runner2, lemenya);
		assertInZone(Zone.SUPPORT, longDark);

		// Pippin's skirmish with the other Goblin Runner is canceled.
		scn.FreepsResolveSkirmish(pippin);
		assertTrue(scn.IsCharSkirmishingAgainst(pippin, runner2));
		scn.FreepsPlayCard(stealth2);
		assertInDiscard(stealth2);
		assertFalse(scn.IsCharSkirmishing(pippin));
		assertFalse(scn.IsCharAssigned(pippin));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(runner2));

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
