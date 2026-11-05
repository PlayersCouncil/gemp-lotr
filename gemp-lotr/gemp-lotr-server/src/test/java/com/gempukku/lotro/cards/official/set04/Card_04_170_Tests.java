package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_170_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ranks", "4_170");
					// Uruk-hai Raiding Party: an [isengard] Uruk-hai, cost 4, site 5; Damage +1 is its whole text.
					put("raiding", "1_158");
					// Rohirrim Traitor: an [isengard] Man, cost 2, site 3; stronger while a companion bears an
					// [isengard] condition, which none does here.
					put("traitor", "6_72");
					put("sentry", "10_84");     // Cirith Ungol Sentry: a [sauron] Uruk-hai, cost 2, site 6; Damage +1 is its whole text
					// Down to the Last Child: an [isengard] condition, cost 2; its text never comes into play here.
					put("child", "4_148");
					// Uruk Foot Soldier: an [isengard] Uruk-hai, cost 1, site 5; laid only in the Free Peoples
					// player's discard pile, a title the Shadow player's pile does not hold.
					put("footsoldier", "4_187");
					put("troop1", "1_177");     // Goblin Patrol Troop: a minion with no text
					put("troop2", "1_177");

					// Elrond, Lord of Rivendell: an [elven] Elf ally whose home is site 3; heals there at the start of
					// his owner's turns, which no test reaches.
					put("elrond", "1_40");
					// Thrarin, Dwarven Smith: a [dwarven] Dwarf ally whose home is site 3; a maneuver ability used
					// only on purpose.
					put("thrarin", "1_27");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion; a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RanksWithoutNumberStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Ranks Without Number
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.<br>Each time the Free Peoples player assigns an ally to a skirmish, you may play an [isengard] minion from your discard pile. That minion's twilight cost is -2.
		*/

		var scn = GetScenario();

		var ranks = scn.GetFreepsCard("ranks");

		assertEquals("Ranks Without Number", ranks.getBlueprint().getTitle());
		assertNull(ranks.getBlueprint().getSubtitle());
		assertTrue(ranks.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, ranks.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, ranks.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, ranks.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(ranks, Keyword.SUPPORT_AREA));
		assertEquals(1, ranks.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachAllyTheFreePeoplesPlayerAssignsLetsAnIsengardMinionBePlayedFromTheDiscardPileForTwoLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveAlliesToTable(elrond, thrarin);

		var ranks = scn.GetShadowCard("ranks");
		var raiding = scn.GetShadowCard("raiding");
		var traitor = scn.GetShadowCard("traitor");
		var sentry = scn.GetShadowCard("sentry");
		var child = scn.GetShadowCard("child");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var theirFootsoldier = scn.GetFreepsCard("footsoldier");
		var ownFootsoldier = scn.GetShadowCard("footsoldier");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToSupportArea(ranks);
		scn.MoveCardsToDiscard(raiding, traitor, sentry, child, theirFootsoldier);
		assertInDiscard(raiding, traitor, sentry, child, theirFootsoldier);
		// The Shadow player's own Foot Soldier is kept out of his discard pile.
		scn.MoveCardsToHand(ownFootsoldier);

		// To site 3, the allies' home.
		scn.FreepsPass();
		scn.MoveMinionsToTable(troop1, troop2);
		scn.SkipToAssignments();
		scn.SetTwilight(4);
		assertEquals(4, scn.GetTwilight());

		// Two allies in one assignment: two offers.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{elrond, troop1},
				new PhysicalCardImpl[]{thrarin, troop2});
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowAcceptOptionalTrigger();

		// An [isengard] Uruk-hai and an [isengard] Man; not a [sauron] Uruk-hai, an [isengard] card that is no
		// minion, or the other player's [isengard] minion in his own pile, each of which the pool would pay for.
		assertTrue(scn.ShadowHasCardChoiceAvailable(raiding, traitor));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sentry, child, theirFootsoldier));
		scn.ShadowChooseCard(raiding);

		assertInZone(Zone.SHADOW_CHARACTERS, raiding);
		// 4 printed - 2 + 2 roaming (site 5 minion at site 3): the pool's 4 exactly.
		assertEquals(0, scn.GetTwilight());

		// The second ally's offer: the Traitor would cost 2 - 2 = 0, so it is made; declined, nothing changes.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowDeclineOptionalTrigger();
		assertInDiscard(traitor, sentry, child);
		assertEquals(0, scn.GetTwilight());

		// The minion played after the Free Peoples player's assignment is no leftover minion (CR 4.2 "assignment
		// phase": leftovers are made only as he finishes), so the skirmishes come next.
		assertFalse(scn.IsCharAssigned(raiding));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void NeitherACompanionTheFreePeoplesPlayerAssignsNorAnAllyTheShadowPlayerAssignsMakesAnOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(elrond);

		var ranks = scn.GetShadowCard("ranks");
		var raiding = scn.GetShadowCard("raiding");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToSupportArea(ranks);
		scn.MoveCardsToDiscard(raiding);
		assertInDiscard(raiding);

		scn.FreepsPass();
		scn.MoveMinionsToTable(troop1, troop2);
		scn.SkipToAssignments();
		scn.SetTwilight(10);
		assertEquals(10, scn.GetTwilight());

		// A companion: no offer, and the Shadow player is asked for his leftover minion.
		scn.FreepsAssignToMinions(aragorn, troop1);
		assertTrue(scn.ShadowDecisionAvailable("Assign minions"));

		// An ally, but assigned by the Shadow player: no offer.
		scn.ShadowAssignToMinions(elrond, troop2);
		assertTrue(scn.IsCharAssignedAgainst(elrond, troop2));
		assertInDiscard(raiding);
		assertEquals(10, scn.GetTwilight());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void IsNotOfferedWithNoIsengardMinionInTheDiscardPileThatTheDiscountedPoolCanPay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveAlliesToTable(elrond);

		var ranks = scn.GetShadowCard("ranks");
		var raiding = scn.GetShadowCard("raiding");
		var sentry = scn.GetShadowCard("sentry");
		var child = scn.GetShadowCard("child");
		var troop1 = scn.GetShadowCard("troop1");
		var theirFootsoldier = scn.GetFreepsCard("footsoldier");
		var ownFootsoldier = scn.GetShadowCard("footsoldier");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToSupportArea(ranks);
		scn.MoveCardsToDiscard(raiding, sentry, child, theirFootsoldier);
		assertInDiscard(raiding, sentry, child, theirFootsoldier);
		scn.MoveCardsToHand(ownFootsoldier);

		scn.FreepsPass();
		scn.MoveMinionsToTable(troop1);
		scn.SkipToAssignments();
		// The Raiding Party would cost 4 - 2 + 2 roaming = 4.  Each card beside it would cost 3 or less if the
		// text's words were wider: the Sentry 2 - 2 + 2, Down to the Last Child 2 - 2, the other player's
		// Foot Soldier 1 - 2 + 2.
		scn.SetTwilight(3);
		assertEquals(3, scn.GetTwilight());

		scn.FreepsAssignToMinions(elrond, troop1);
		assertTrue(scn.IsCharAssignedAgainst(elrond, troop1));
		assertInDiscard(raiding, sentry, child, theirFootsoldier);
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}
}
