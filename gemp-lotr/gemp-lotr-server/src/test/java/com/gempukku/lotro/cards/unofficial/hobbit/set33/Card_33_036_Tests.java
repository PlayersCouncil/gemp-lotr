package com.gempukku.lotro.cards.unofficial.hobbit.set33;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_33_036_Tests
{

	// The Hobbit table: the Hobbit path, Bilbo, Reliable Companion 30_46 as the Ring-bearer and no card in the Ring
	// slot.  The skirmishes are those of turn 1, at The Troll Hoard 30_50 (site 2); the minions are cheated in.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("troll", "33_36");
					// A second copy: its own exertion, and a minion that is no Orc.
					put("troll2", "33_36");
					// Orcs of two cultures.
					put("footman", "30_42");     // Goblin Footman: [moria] Orc; a "when you play" trigger, not reached
					put("aggressor", "30_39");   // Orkish Aggressor: [gundabad] Orc; an Assignment ability, not used
					// Demolition Troll: a [moria] Troll, no Orc.  A Skirmish ability, not used.
					put("demolition", "32_31");
					// Thorin, Oakenshield: [dwarven] Dwarf companion, to bear the Axe and Dori.
					put("thorin", "30_19");
					// Balin, Brother of Dwalin: [dwarven] Dwarf companion, to bear the Iron-Forged Weapon.
					put("balin", "30_2");
					// [dwarven] possessions: borne, and in the support area.
					put("axe", "31_1");          // Dwarven Axe: hand weapon on a [dwarven] character
					put("barricade", "31_4");    // Great Barricade: support area; its texts are at sites 7 and 8 and on a regroup move
					// [dwarven] artifacts: borne, and in the support area.
					put("coat", "31_5");         // Mithril Coat: armor on Bilbo
					put("arkenstone", "31_7");   // The Arkenstone: support area; Fellowship and Regroup abilities
					// Cards that are not a [dwarven] possession or artifact.
					put("iron", "32_18");        // Iron-Forged Weapon: [esgaroth] possession; a Skirmish ability, not used
					put("sting", "30_47");       // Sting: [shire] artifact on Bilbo; a skirmish-win trigger, not reached
					put("dori", "30_8");         // Dori: [dwarven] follower; Aid, declined by the skippers
				}},
				null,
				null,
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void BridgingTrollStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 33
		 * Name: Bridging Troll
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 5
		 * Type: Minion
		 * Subtype: Troll
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 4
		 * Game Text: <b>Skirmish:</b> Exert this minion and discard an Orc to make the Free Peoples player discard a [dwarven] possession or [dwarven] artifact.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("troll");

		assertEquals("Bridging Troll", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.TROLL, card.getBlueprint().getRace());
		assertEquals(5, card.getBlueprint().getTwilightCost());
		assertEquals(10, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void SkirmishExertsThisMinionAndDiscardsAnOrcToMakeTheFreePeoplesPlayerDiscardADwarvenPossessionOrArtifact() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll");
		var troll2 = scn.GetShadowCard("troll2");
		var footman = scn.GetShadowCard("footman");
		var aggressor = scn.GetShadowCard("aggressor");
		var demolition = scn.GetShadowCard("demolition");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var axe = scn.GetFreepsCard("axe");
		var barricade = scn.GetFreepsCard("barricade");
		var coat = scn.GetFreepsCard("coat");
		var arkenstone = scn.GetFreepsCard("arkenstone");
		var iron = scn.GetFreepsCard("iron");
		var sting = scn.GetFreepsCard("sting");
		var dori = scn.GetFreepsCard("dori");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thorin, balin);
		scn.AttachCardsTo(thorin, axe, dori);
		scn.AttachCardsTo(balin, iron);
		scn.AttachCardsTo(bilbo, coat, sting);
		scn.MoveCardsToSupportArea(barricade, arkenstone);

		scn.StartGame();
		scn.MoveMinionsToTable(troll, troll2, footman, aggressor, demolition);
		assertInZone(Zone.SHADOW_CHARACTERS, troll, troll2, footman, aggressor, demolition);
		assertAttachedTo(axe, thorin);
		assertAttachedTo(dori, thorin);
		assertAttachedTo(iron, balin);
		assertAttachedTo(coat, bilbo);
		assertAttachedTo(sting, bilbo);
		assertInZone(Zone.SUPPORT, barricade, arkenstone);
		assertEquals(0, scn.GetWoundsOn(troll));
		assertEquals(0, scn.GetWoundsOn(troll2));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thorin, troll);
		// The other minions are left over.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(thorin);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(troll));
		scn.ShadowUseCardAction(troll);

		// This copy exerts, and not the other.
		assertEquals(1, scn.GetWoundsOn(troll));
		assertEquals(0, scn.GetWoundsOn(troll2));
		// Orcs of two cultures; not the Trolls.
		assertTrue(scn.ShadowHasCardChoiceAvailable(footman, aggressor));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troll, troll2, demolition));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(footman);
		assertInZone(Zone.DISCARD, footman);
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor, troll2, demolition);

		// The Free Peoples player chooses: [dwarven] possessions and artifacts, borne and in the support area; not
		// a possession or artifact of another culture, nor a [dwarven] follower.
		assertFalse(scn.ShadowDecisionAvailable("discard"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(axe, barricade, coat, arkenstone));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(iron, sting, dori));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(axe);

		assertInZone(Zone.DISCARD, axe);
		assertAttachedTo(dori, thorin);
		assertAttachedTo(iron, balin);
		assertAttachedTo(coat, bilbo);
		assertAttachedTo(sting, bilbo);
		assertInZone(Zone.SUPPORT, barricade, arkenstone);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoDwarvenPossessionOrArtifactInPlayTheCostIsPaidAndNothingIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll");
		var footman = scn.GetShadowCard("footman");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var iron = scn.GetFreepsCard("iron");
		var sting = scn.GetFreepsCard("sting");
		var dori = scn.GetFreepsCard("dori");
		var bilbo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(thorin, balin);
		scn.AttachCardsTo(thorin, dori);
		scn.AttachCardsTo(balin, iron);
		scn.AttachCardsTo(bilbo, sting);

		scn.StartGame();
		scn.MoveMinionsToTable(troll, footman);
		assertInZone(Zone.SHADOW_CHARACTERS, troll, footman);
		assertAttachedTo(dori, thorin);
		assertAttachedTo(iron, balin);
		assertAttachedTo(sting, bilbo);
		assertEquals(0, scn.GetWoundsOn(troll));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thorin, troll);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(thorin);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(troll));
		scn.ShadowUseCardAction(troll);

		// The one Orc is discarded unasked; the Free Peoples player has nothing to choose.
		assertEquals(1, scn.GetWoundsOn(troll));
		assertInZone(Zone.DISCARD, footman);
		assertAttachedTo(dori, thorin);
		assertAttachedTo(iron, balin);
		assertAttachedTo(sting, bilbo);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishActionIsNotOfferedWhileThisMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll");
		var footman = scn.GetShadowCard("footman");
		var thorin = scn.GetFreepsCard("thorin");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(thorin);
		scn.AttachCardsTo(thorin, axe);

		scn.StartGame();
		scn.MoveMinionsToTable(troll, footman);
		// 2 wounds of 3: no exertion left.
		scn.AddWoundsToChar(troll, 2);
		assertInZone(Zone.SHADOW_CHARACTERS, troll, footman);
		assertAttachedTo(axe, thorin);
		assertEquals(2, scn.GetWoundsOn(troll));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thorin, troll);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(thorin);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(troll));

		// One wound off, and it is offered.
		scn.RemoveWoundsFromChar(troll, 1);
		scn.ShadowRefreshDecision();
		assertEquals(1, scn.GetWoundsOn(troll));
		assertTrue(scn.ShadowActionAvailable(troll));
	}

	@Test
	public void SkirmishActionIsNotOfferedWithNoOrcToDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var troll = scn.GetShadowCard("troll");
		var demolition = scn.GetShadowCard("demolition");
		var aggressor = scn.GetShadowCard("aggressor");
		var thorin = scn.GetFreepsCard("thorin");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(thorin);
		scn.AttachCardsTo(thorin, axe);

		scn.StartGame();
		scn.MoveMinionsToTable(troll, demolition);
		assertInZone(Zone.SHADOW_CHARACTERS, troll, demolition);
		assertAttachedTo(axe, thorin);
		assertEquals(0, scn.GetWoundsOn(troll));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thorin, troll);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(thorin);
		scn.FreepsPass();

		// The Troll could exert, and a [moria] minion that is no Orc stands beside it.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(troll));

		// An Orc in play, and it is offered.
		scn.MoveMinionsToTable(aggressor);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor);
		assertTrue(scn.ShadowActionAvailable(troll));
	}
}
