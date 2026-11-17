package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_118_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("omen", "15_118");
					put("goblin", "11_137");     // Scurrying Goblin: [orc] Orc, cost 1, site 4; adds (2) only when played underground
					put("goblin2", "11_137");
					put("denizen", "11_115");    // Denizen of Khazad-dûm: [orc] Orc, site 4; strength +2 only underground
					put("denizen2", "11_115");
					put("halftroll", "14_12");   // Half-troll of Far Harad: an [orc] minion who is no Orc, site 4; a start-of-assignment trigger only
					put("chieftain", "1_266");   // Orc Chieftain: a [sauron] Orc, site 6; answers only a weapon played on him

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability used only on purpose
					put("gimli", "1_13");        // Gimli, Son of Glóin: [dwarven] Dwarf companion; a skirmish ability
					put("boromir", "1_97");      // Boromir, Son of Denethor: [gondor] Man companion; a skirmish ability used only on purpose
					put("elrond", "1_40");       // Elrond, Lord of Rivendell: an ally, no companion; a fellowship ability used only on purpose
					put("sting", "18_113");      // Sting, Elven Long Knife: on Frodo, "Regroup: Exert bearer to remove a threat"
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UnmistakableOmenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Unmistakable Omen
		 * Unique: True
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: While you can spot a threat, the site number of each [orc] Orc is -1.<br><b>Regroup:</b> Remove 3 threats and discard 3 [orc] Orcs from play to make the Free Peoples player spot a companion and place that companion in the dead pile. Discard this condition.
		*/

		var scn = GetScenario();

		var omen = scn.GetFreepsCard("omen");

		assertEquals("Unmistakable Omen", omen.getBlueprint().getTitle());
		assertNull(omen.getBlueprint().getSubtitle());
		assertTrue(omen.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, omen.getBlueprint().getSide());
		assertEquals(Culture.ORC, omen.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, omen.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(omen, Keyword.SUPPORT_AREA));
		assertEquals(1, omen.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhileAThreatCanBeSpottedEachOrcOrcIsSiteNumberMinusOneAndPlaysAtSite3WithoutRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var omen = scn.GetShadowCard("omen");
		var goblin = scn.GetShadowCard("goblin");
		var denizen = scn.GetShadowCard("denizen");
		var halftroll = scn.GetShadowCard("halftroll");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCardsToSupportArea(omen);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, omen);
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(denizen, halftroll, chieftain);
		scn.MoveCardsToHand(goblin);
		assertInHand(goblin);
		// A burden, the nearest token to a threat, and no threat.
		scn.AddBurdens(1);
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetThreats());
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(4, scn.GetMinionSiteNumber(denizen));
		assertEquals(4, scn.GetMinionSiteNumber(goblin));
		assertEquals(4, scn.GetMinionSiteNumber(halftroll));
		assertEquals(6, scn.GetMinionSiteNumber(chieftain));
		assertTrue(scn.HasKeyword(denizen, Keyword.ROAMING));

		scn.AddThreats(1);
		scn.ShadowRefreshDecision();
		assertEquals(1, scn.GetThreats());
		// The [orc] Orcs, in play and in hand; not the [orc] Half-troll, nor the [sauron] Orc.
		assertEquals(3, scn.GetMinionSiteNumber(denizen));
		assertEquals(3, scn.GetMinionSiteNumber(goblin));
		assertEquals(4, scn.GetMinionSiteNumber(halftroll));
		assertEquals(6, scn.GetMinionSiteNumber(chieftain));
		assertFalse(scn.HasKeyword(denizen, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(halftroll, Keyword.ROAMING));

		int pool = scn.GetTwilight();
		scn.ShadowPlayCard(goblin);
		// 1 to play and no roaming penalty: at site number 4 it would have been 1 + 2.
		assertEquals(pool - 1, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, goblin);
		assertFalse(scn.HasKeyword(goblin, Keyword.ROAMING));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheSiteNumberIsBackOnceTheLastThreatIsRemoved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sting = scn.GetFreepsCard("sting");
		scn.AttachCardsTo(frodo, sting);

		var omen = scn.GetShadowCard("omen");
		var denizen = scn.GetShadowCard("denizen");
		scn.MoveCardsToSupportArea(omen);

		scn.StartGame();
		assertAttachedTo(sting, frodo);
		assertInZone(Zone.SUPPORT, omen);
		scn.MoveMinionsToTable(denizen);
		scn.AddThreats(1);
		scn.FreepsRefreshDecision();
		assertEquals(1, scn.GetThreats());
		assertEquals(3, scn.GetMinionSiteNumber(denizen));

		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetMinionSiteNumber(denizen));
		assertTrue(scn.FreepsActionAvailable(sting));

		scn.FreepsUseCardAction(sting);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetThreats());
		assertEquals(4, scn.GetMinionSiteNumber(denizen));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityRemovesThreeThreatsAndDiscardsThreeOrcOrcsToHaveTheFreePeoplesPlayerKillACompanionOfHisChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var boromir = scn.GetFreepsCard("boromir");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(aragorn, gimli, boromir);
		scn.MoveAlliesToTable(elrond);

		var omen = scn.GetShadowCard("omen");
		var goblin = scn.GetShadowCard("goblin");
		var goblin2 = scn.GetShadowCard("goblin2");
		var denizen = scn.GetShadowCard("denizen");
		var denizen2 = scn.GetShadowCard("denizen2");
		var halftroll = scn.GetShadowCard("halftroll");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCardsToSupportArea(omen);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, omen);
		scn.MoveMinionsToTable(goblin, goblin2, denizen, denizen2, halftroll, chieftain);
		// Four companions: three threats can lie.
		scn.AddThreats(3);
		scn.FreepsRefreshDecision();
		assertEquals(3, scn.GetThreats());

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertEquals(3, scn.GetThreats());
		assertEquals(0, scn.GetFreepsDeadCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(omen));

		scn.ShadowUseCardAction(omen);
		// [orc] Orcs only: not the [orc] Half-troll, nor the [sauron] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(goblin, goblin2, denizen, denizen2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(halftroll, chieftain));
		scn.ShadowChooseCards(goblin, goblin2, denizen);
		assertEquals(0, scn.GetThreats());
		assertInDiscard(goblin);
		assertInDiscard(goblin2);
		assertInDiscard(denizen);
		assertInZone(Zone.SHADOW_CHARACTERS, denizen2);
		assertInZone(Zone.SHADOW_CHARACTERS, halftroll);
		assertInZone(Zone.SHADOW_CHARACTERS, chieftain);

		// The Free Peoples player chooses: any companion, the Ring-bearer among them; not an ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, gimli, boromir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(elrond));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(gimli);
		assertInZone(Zone.DEAD, gimli);
		assertEquals(1, scn.GetFreepsDeadCount());
		// The threats went with the cost, so the kill leaves no threat wounds to place.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInDiscard(omen);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWithTwoThreats() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("gimli"), scn.GetFreepsCard("boromir"));

		var omen = scn.GetShadowCard("omen");
		var goblin = scn.GetShadowCard("goblin");
		var goblin2 = scn.GetShadowCard("goblin2");
		var denizen = scn.GetShadowCard("denizen");
		scn.MoveCardsToSupportArea(omen);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, omen);
		scn.MoveMinionsToTable(goblin, goblin2, denizen);
		scn.AddThreats(2);
		scn.FreepsRefreshDecision();
		assertEquals(2, scn.GetThreats());

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(omen));
	}

	@Test
	public void RegroupAbilityIsNotOfferedWithTwoOrcOrcsBesideAnOrcHalfTrollAndASauronOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		scn.MoveCompanionsToTable(scn.GetFreepsCard("aragorn"), scn.GetFreepsCard("gimli"), scn.GetFreepsCard("boromir"));

		var omen = scn.GetShadowCard("omen");
		var goblin = scn.GetShadowCard("goblin");
		var denizen = scn.GetShadowCard("denizen");
		var halftroll = scn.GetShadowCard("halftroll");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCardsToSupportArea(omen);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, omen);
		scn.MoveMinionsToTable(goblin, denizen, halftroll, chieftain);
		scn.AddThreats(3);
		scn.FreepsRefreshDecision();
		assertEquals(3, scn.GetThreats());

		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPass();
		assertEquals(3, scn.GetThreats());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(omen));
	}
}
