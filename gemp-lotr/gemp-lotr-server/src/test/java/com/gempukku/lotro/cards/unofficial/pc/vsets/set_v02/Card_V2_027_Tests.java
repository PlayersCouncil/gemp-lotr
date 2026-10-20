package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_027_Tests
{

	// The Towers sites: White Rocks (4) and Deeping Wall (5) are battlegrounds, and sites 1, 2, 3 and 6 are not.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("legion", "102_27");

					put("savage", "1_151");      // Uruk Savage: an [isengard] Uruk-hai, strength 5, vitality 3, damage +1
					// Isengard Grunt: "Skirmish: Exert this minion to prevent an [isengard] Orc from being
					// overwhelmed unless its strength is tripled."  Offered in any skirmish while it can exert.
					put("grunt", "102_26");
					// Down to the Last Child: "Shadow: Remove 2 [isengard] tokens from this card to take control
					// of a site."  Its other text needs a companion to lose to an Uruk-hai.
					put("lastchild", "4_148");
					// Uruk Bloodlust: "Response: If your Uruk-hai wins a skirmish, remove (1) to heal him."
					put("bloodlust", "1_144");
					// Ferocity: "Skirmish: Make an Uruk-hai strength +2 (or +3 if at a battleground)."
					put("ferocity", "4_151");
					// Strength Born of Fear: a [sauron] condition in the support area, with no other text.
					// "Skirmish: Remove (3) to make a [sauron] Orc strength +1."
					put("fear", "1_278");

					// Gimli, Son of Gloin: strength 6, damage +1.  "Skirmish: Exert Gimli to make him strength +2."
					put("gimli", "1_13");
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: strength 8, vitality 4
					// Bounder: an ally.  "Skirmish: Exert this ally to prevent a Hobbit from being overwhelmed
					// unless that Hobbit's strength is tripled."
					put("bounder", "1_286");
					// Axe Strike: "Skirmish: Make a Dwarf strength +2 (or +3 if bearing a [dwarven] hand weapon)."
					put("axestrike", "1_3");
					// Endurance of Dwarves: a condition a Dwarf bears, with no other text.  "Skirmish: Discard this
					// condition to make bearer strength +1."
					put("endurance", "2_4");
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LegionofIsengardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Legion of Isengard
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 8
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 17
		 * Vitality: 4
		 * Site Number: 5
		 * Game Text: Damage +1.
		 * 	While you control a battleground, this minion is damage +1.
		 * 	While at a battleground, skirmish special abilities cannot be used during skirmishes involving this minion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("legion");

		assertEquals("Legion of Isengard", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(8, card.getBlueprint().getTwilightCost());
		assertEquals(17, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsDamagePlus2OnlyOnceASiteItsOwnerControlsIsABattleground() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legion = scn.GetShadowCard("legion");
		var savage = scn.GetShadowCard("savage");
		var lastChild = scn.GetShadowCard("lastchild");

		scn.StartGame();
		scn.SkipToSite(5);

		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		var site3 = scn.GetSite(3);
		var site4 = scn.GetSite(4);
		scn.MoveMinionsToTable(legion, savage);
		scn.MoveCardsToSupportArea(lastChild);
		// Enough to take control of four sites, one at a time and lowest first.
		scn.AddTokensToCard(lastChild, 8);

		// The fellowship is at Deeping Wall, a battleground, and the Shadow player controls nothing.
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(1, scn.GetKeywordCount(legion, Keyword.DAMAGE));

		scn.FreepsPass();
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));

		scn.ShadowUseCardAction(lastChild);
		scn.ShadowUseCardAction(lastChild);
		scn.ShadowUseCardAction(lastChild);
		// Three sites controlled, and none of them a battleground.
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.ShadowControls(site2));
		assertTrue(scn.ShadowControls(site3));
		assertFalse(scn.HasKeyword(site1, Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(site2, Keyword.BATTLEGROUND));
		assertFalse(scn.HasKeyword(site3, Keyword.BATTLEGROUND));
		assertFalse(scn.IsSiteControlled(site4));
		assertEquals(1, scn.GetKeywordCount(legion, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(savage, Keyword.DAMAGE));

		scn.ShadowUseCardAction(lastChild);
		// The fourth is White Rocks.
		assertTrue(scn.ShadowControls(site4));
		assertTrue(scn.HasKeyword(site4, Keyword.BATTLEGROUND));
		assertEquals(0, scn.GetCultureTokensOn(lastChild));
		// The printed damage +1 and one more.
		assertEquals(2, scn.GetKeywordCount(legion, Keyword.DAMAGE));
		// "This minion": another damage +1 [isengard] Uruk-hai gains nothing.
		assertEquals(1, scn.GetKeywordCount(savage, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ABattlegroundTheOtherPlayerControlsGivesNoDamageBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var theirLegion = scn.GetFreepsCard("legion");

		scn.StartGame();
		scn.SkipToSite(5);

		var site4 = scn.GetSite(4);
		// Player 2, the Shadow player this turn, controls sites 1 to 4, lowest first.
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(site4));
		assertTrue(scn.HasKeyword(site4, Keyword.BATTLEGROUND));

		// On player 2's own turn player 1 is the Shadow player, and the Legion on the table is player 1's.
		scn.SkipToOtherPlayersTurn();
		scn.ShadowPass();
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
		scn.MoveMinionsToTable(theirLegion);

		assertTrue(scn.ShadowControls(site4));
		assertFalse(scn.FreepsControls(site4));
		assertEquals(0, scn.GetFreepsControlledSiteCount());
		assertInZone(Zone.SHADOW_CHARACTERS, theirLegion);
		assertEquals(1, scn.GetKeywordCount(theirLegion, Keyword.DAMAGE));
		assertTrue(scn.FreepsDecisionAvailable("Play Shadow action or Pass"));
	}

	@Test
	public void AtABattlegroundSkirmishSpecialAbilitiesCannotBeUsedInItsSkirmishAndEventsAndResponsesCan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legion = scn.GetShadowCard("legion");
		var savage = scn.GetShadowCard("savage");
		var grunt = scn.GetShadowCard("grunt");
		var bloodlust = scn.GetShadowCard("bloodlust");
		var ferocity = scn.GetShadowCard("ferocity");
		var fear = scn.GetShadowCard("fear");
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var axeStrike = scn.GetFreepsCard("axestrike");
		var endurance = scn.GetFreepsCard("endurance");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.AttachCardsTo(gimli, endurance);

		scn.StartGame();
		scn.SkipToSite(3);

		scn.MoveCardsToHand(axeStrike, ferocity);
		scn.MoveMinionsToTable(legion, savage, grunt);
		scn.MoveCardsToSupportArea(bloodlust, fear);
		scn.AddWoundsToChar(legion, 1);
		scn.FreepsPass();

		// White Rocks, a battleground.  The Shadow player controls no site.
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertTrue(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertEquals(1, scn.GetKeywordCount(legion, Keyword.DAMAGE));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage}, new PhysicalCardImpl[]{gimli, legion});
		// The Grunt stays out of both skirmishes.
		scn.ShadowDeclineAssignments();

		// Aragorn's skirmish with the Savage comes first.  The Legion is assigned and waiting, and every
		// skirmish special ability on the table is on offer.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharAssigned(legion));
		assertFalse(scn.IsCharSkirmishing(legion));
		assertTrue(scn.FreepsActionAvailable(gimli));
		assertTrue(scn.FreepsActionAvailable(bounder));
		assertTrue(scn.FreepsActionAvailable(endurance));
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(grunt));
		// 3 companions + 3 for White Rocks: enough for the 3 that Strength Born of Fear asks.
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.ShadowActionAvailable(fear));
		scn.ShadowPass();

		// Gimli's skirmish with the Legion.
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.IsCharSkirmishing(legion));
		// Not a companion's in the skirmish, not an ally's outside it, and not a condition's: the ability
		// need not be a character's.
		assertFalse(scn.FreepsActionAvailable(gimli));
		assertFalse(scn.FreepsActionAvailable(bounder));
		assertFalse(scn.FreepsActionAvailable(endurance));
		// A skirmish event is no special ability.
		assertTrue(scn.FreepsPlayAvailable(axeStrike));
		scn.FreepsPlayCard(axeStrike);
		// 6 + 2
		assertEquals(8, scn.GetStrength(gimli));

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		// Not a Shadow minion's either, nor a Shadow condition's in the support area.
		assertFalse(scn.ShadowActionAvailable(grunt));
		assertFalse(scn.ShadowActionAvailable(fear));
		assertTrue(scn.ShadowPlayAvailable(ferocity));
		scn.ShadowPlayCard(ferocity);
		scn.ShadowChooseCard(legion);
		// 17 + 3 at a battleground
		assertEquals(20, scn.GetStrength(legion));

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(gimli));
		assertFalse(scn.FreepsActionAvailable(bounder));
		assertFalse(scn.FreepsActionAvailable(endurance));
		scn.FreepsPass();
		assertFalse(scn.ShadowActionAvailable(grunt));
		assertFalse(scn.ShadowActionAvailable(fear));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		// 3 companions + 3 for White Rocks, and neither event cost anything
		assertEquals(6, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(legion));
		scn.ShadowPass();

		// The Legion wins, 20 to 8.  A Response is a special ability and not a skirmish one: it is offered
		// while the Legion's skirmish is still going on.
		// Engine pass 54: what answers the win comes before the loser loses.
		assertInZone(Zone.FREE_CHARACTERS, gimli);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(bloodlust));
		scn.ShadowChooseOptionalTrigger(bloodlust);
		assertEquals(5, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(legion));
		// A player may respond more than once to the same win.  Once is enough here.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(bloodlust));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AwayFromABattlegroundSkirmishSpecialAbilitiesAreUsedInItsSkirmishThoughItsOwnerControlsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legion = scn.GetShadowCard("legion");
		var grunt = scn.GetShadowCard("grunt");
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		scn.SkipToSite(5);

		var site4 = scn.GetSite(4);
		scn.MoveMinionsToTable(legion, grunt);
		// Sites 1 to 4, lowest first.
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.FreepsPass();

		// The Shadow player controls White Rocks, a battleground, and the fellowship is at Caves of Aglarond,
		// which is none.
		assertTrue(scn.ShadowControls(site4));
		assertTrue(scn.HasKeyword(site4, Keyword.BATTLEGROUND));
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertFalse(scn.HasKeyword(scn.GetCurrentSite(), Keyword.BATTLEGROUND));
		assertEquals(2, scn.GetKeywordCount(legion, Keyword.DAMAGE));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(gimli, legion);
		scn.ShadowDeclineAssignments();

		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.IsCharSkirmishing(legion));
		assertTrue(scn.FreepsActionAvailable(bounder));
		assertTrue(scn.FreepsActionAvailable(gimli));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(6, scn.GetStrength(gimli));

		scn.FreepsUseCardAction(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(8, scn.GetStrength(gimli));

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(grunt));
		assertEquals(0, scn.GetWoundsOn(grunt));
		scn.ShadowUseCardAction(grunt);
		assertEquals(1, scn.GetWoundsOn(grunt));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
