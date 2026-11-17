package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_088_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shield", "8_88");
					// Éowyn, Daughter of Éomund: [rohan] Man companion, strength 6, vitality 3; a maneuver ability used
					// only on purpose
					put("eowyn", "5_122");
					put("sword", "4_272");      // Éowyn's Sword: a [rohan] hand weapon on Éowyn; damage +1 only with a villager
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion, vitality 3; valiant is its whole text
					// Rohirrim Herdsman: a [rohan] Man ally, vitality 2; a regroup ability used only on purpose
					put("herdsman", "7_247");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man; a maneuver ability used only on purpose
					// Merry, Master Holbytla: a [rohan] Hobbit, a Man only while 3 [rohan] companions or Éowyn can be
					// spotted (with the Guard alone, two); his trigger answers a [rohan] Man exerting, which nobody does here
					put("merry", "103_85");
					put("legolas", "1_50");     // Legolas, Greenleaf: an archer; his archery ability is used only on purpose

					put("marksman1", "1_176");  // Goblin Marksman: a [moria] archer minion; Archer is its whole text
					put("marksman2", "1_176");
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, strength 8, no text
					put("brigand2", "4_10");
					put("axe", "4_26");         // Iron Axe: [dunland] hand weapon, strength +3, on a [dunland] Man; no other text
					put("axe2", "4_26");
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5; its play trigger does not fire when cheated in
					put("scimitar", "1_180");   // Goblin Scimitar: [moria] hand weapon, strength +2, on a [moria] Orc; a play trigger only
					put("balrog", "12_79");     // The Balrog, The Terror of Khazad-dûm: strength 17; fierce only underground
					put("balrogsword", "2_50"); // The Balrog's Sword: an ARTIFACT hand weapon, on The Balrog
					// Ted Sandyman, Chief's Men's Ally: a [men] minion; abilities used only on purpose
					put("ted", "18_75");
					// Long Battle Bow: a RANGED weapon on a [men] minion, which makes him an archer; a play trigger only
					put("bow", "11_89");
					put("denizen", "11_116");   // Denizen of Moria: [orc] Orc, strength 7; +3 only at an underground site
					put("warg", "17_92");       // Vicious Warg: a mount, no weapon, on an [orc] Orc; makes its bearer fierce
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EowynsShieldStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Éowyn's Shield
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Shield
		 * Game Text: Bearer must be a [rohan] Man.<br>The minion archery total is -1.<br><b>Skirmish:</b> If bearer is Éowyn, add a threat and discard Éowyn's Shield to discard a hand weapon borne by a minion she is skirmishing.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("shield");

		assertEquals("Éowyn's Shield", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.SHIELD));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnARohanManCompanionOrAllyAndMakesTheMinionArcheryTotalMinusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shield = scn.GetFreepsCard("shield");
		var guard = scn.GetFreepsCard("guard");
		var herdsman = scn.GetFreepsCard("herdsman");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var legolas = scn.GetFreepsCard("legolas");
		scn.MoveCompanionsToTable(guard, aragorn, merry, legolas);
		scn.MoveAlliesToTable(herdsman);

		var marksman1 = scn.GetShadowCard("marksman1");
		var marksman2 = scn.GetShadowCard("marksman2");

		scn.StartGame();
		scn.MoveCardsToHand(shield);
		assertInHand(shield);
		// The minions stand on the table later in the turn; laid now, the totals are read before and after the play.
		scn.MoveMinionsToTable(marksman1, marksman2);
		scn.FreepsRefreshDecision();

		// Two Shadow archers, one Free Peoples archer.
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertEquals(1, scn.GetFreepsArcheryTotal());

		scn.FreepsPlayCard(shield);
		// A [rohan] Man companion and a [rohan] Man ally; not a [gondor] Man, not a [rohan] Hobbit, not an Elf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, herdsman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, merry, legolas));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(herdsman);

		assertAttachedTo(shield, herdsman);
		// The minion archery total goes down by one; the fellowship's does not.
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// And so it stands when the archery phase comes.
		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void SkirmishAbilityAddsAThreatAndDiscardsTheShieldToDiscardAHandWeaponOfAMinionEowynIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shield = scn.GetFreepsCard("shield");
		var eowyn = scn.GetFreepsCard("eowyn");
		var sword = scn.GetFreepsCard("sword");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(eowyn, aragorn);
		scn.AttachCardsTo(eowyn, shield, sword);
		assertAttachedTo(shield, eowyn);
		assertAttachedTo(sword, eowyn);

		var brigand = scn.GetShadowCard("brigand");
		var brigand2 = scn.GetShadowCard("brigand2");
		var axe = scn.GetShadowCard("axe");
		var axe2 = scn.GetShadowCard("axe2");
		var runner = scn.GetShadowCard("runner");
		var scimitar = scn.GetShadowCard("scimitar");
		var balrog = scn.GetShadowCard("balrog");
		var balrogsword = scn.GetShadowCard("balrogsword");
		var ted = scn.GetShadowCard("ted");
		var bow = scn.GetShadowCard("bow");
		var denizen = scn.GetShadowCard("denizen");
		var warg = scn.GetShadowCard("warg");

		scn.StartGame();
		scn.MoveMinionsToTable(brigand, brigand2, runner, balrog, ted, denizen);
		scn.AttachCardsTo(brigand, axe);
		scn.AttachCardsTo(brigand2, axe2);
		scn.AttachCardsTo(runner, scimitar);
		scn.AttachCardsTo(balrog, balrogsword);
		// The bow makes Ted an archer: 1 to the minion archery total, which the Shield takes off again.
		scn.AttachCardsTo(ted, bow);
		scn.AttachCardsTo(denizen, warg);
		assertAttachedTo(axe, brigand);
		assertAttachedTo(axe2, brigand2);
		assertAttachedTo(scimitar, runner);
		assertAttachedTo(balrogsword, balrog);
		assertAttachedTo(bow, ted);
		assertAttachedTo(warg, denizen);
		assertEquals(0, scn.GetThreats());

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{eowyn, brigand},
				new PhysicalCardImpl[]{aragorn, brigand2});
		// The Shadow player adds the other four to Éowyn.
		scn.ShadowAssignToMinions(eowyn, runner, balrog, ted, denizen);

		scn.FreepsResolveSkirmish(eowyn);
		assertTrue(scn.IsCharSkirmishingAgainst(eowyn, brigand));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(shield));
		scn.FreepsUseCardAction(shield);

		// Hand weapons of two cultures, a possession and an artifact, each borne by a minion she is skirmishing;
		// not a ranged weapon nor a mount borne there, not her own hand weapon, and not the axe of the minion
		// skirmishing Aragorn.
		assertTrue(scn.FreepsHasCardChoiceAvailable(axe, scimitar, balrogsword));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(bow, warg, sword, axe2));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(scimitar);

		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.DISCARD, shield);
		assertInZone(Zone.DISCARD, scimitar);
		assertAttachedTo(axe, brigand);
		assertAttachedTo(balrogsword, balrog);
		assertAttachedTo(bow, ted);
		assertAttachedTo(warg, denizen);
		assertAttachedTo(sword, eowyn);
		assertAttachedTo(axe2, brigand2);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void UsedInAnotherCompanionsSkirmishItIsPaidForAndDiscardsNoWeapon() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shield = scn.GetFreepsCard("shield");
		var eowyn = scn.GetFreepsCard("eowyn");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(eowyn, aragorn);
		scn.AttachCardsTo(eowyn, shield);
		assertAttachedTo(shield, eowyn);

		var brigand = scn.GetShadowCard("brigand");
		var brigand2 = scn.GetShadowCard("brigand2");
		var axe = scn.GetShadowCard("axe");
		var axe2 = scn.GetShadowCard("axe2");

		scn.StartGame();
		scn.MoveMinionsToTable(brigand, brigand2);
		scn.AttachCardsTo(brigand, axe);
		scn.AttachCardsTo(brigand2, axe2);
		assertAttachedTo(axe, brigand);
		assertAttachedTo(axe2, brigand2);
		assertEquals(0, scn.GetThreats());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{eowyn, brigand},
				new PhysicalCardImpl[]{aragorn, brigand2});

		// Aragorn's skirmish first, while Éowyn bears the Shield and waits for her own.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, brigand2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		// Its bearer is Éowyn and its cost can be paid, so it is offered; a minion she is skirmishing there is none.
		assertTrue(scn.FreepsActionAvailable(shield));
		scn.FreepsUseCardAction(shield);

		assertEquals(1, scn.GetThreats());
		assertInZone(Zone.DISCARD, shield);
		assertAttachedTo(axe2, brigand2);
		assertAttachedTo(axe, brigand);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void BorneByARohanManWhoIsNotEowynItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shield = scn.GetFreepsCard("shield");
		var eowyn = scn.GetFreepsCard("eowyn");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(eowyn, guard);
		scn.AttachCardsTo(guard, shield);
		assertAttachedTo(shield, guard);

		var brigand = scn.GetShadowCard("brigand");
		var brigand2 = scn.GetShadowCard("brigand2");
		var axe = scn.GetShadowCard("axe");
		var axe2 = scn.GetShadowCard("axe2");

		scn.StartGame();
		scn.MoveMinionsToTable(brigand, brigand2);
		scn.AttachCardsTo(brigand, axe);
		scn.AttachCardsTo(brigand2, axe2);
		assertAttachedTo(axe, brigand);
		assertAttachedTo(axe2, brigand2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{guard, brigand},
				new PhysicalCardImpl[]{eowyn, brigand2});

		// Éowyn is on the table and the Guard bears the Shield: in either skirmish, it is not offered.
		scn.FreepsResolveSkirmish(guard);
		assertTrue(scn.IsCharSkirmishingAgainst(guard, brigand));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(shield));
		scn.PassSkirmishActions();

		scn.FreepsResolveSkirmish(eowyn);
		assertTrue(scn.IsCharSkirmishingAgainst(eowyn, brigand2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(shield));
		assertAttachedTo(shield, guard);
		assertAttachedTo(axe2, brigand2);
	}

	@Test
	public void WithAsManyThreatsAsCompanionsItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shield = scn.GetFreepsCard("shield");
		var eowyn = scn.GetFreepsCard("eowyn");
		scn.MoveCompanionsToTable(eowyn);
		scn.AttachCardsTo(eowyn, shield);
		assertAttachedTo(shield, eowyn);

		var brigand = scn.GetShadowCard("brigand");
		var axe = scn.GetShadowCard("axe");

		scn.StartGame();
		// Two companions, Frodo and Éowyn: two threats are as many as there can be.
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());
		scn.MoveMinionsToTable(brigand);
		scn.AttachCardsTo(brigand, axe);
		assertAttachedTo(axe, brigand);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(eowyn, brigand);
		scn.FreepsResolveSkirmish(eowyn);

		assertTrue(scn.IsCharSkirmishingAgainst(eowyn, brigand));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(shield));
		assertEquals(2, scn.GetThreats());
		assertAttachedTo(shield, eowyn);
		assertAttachedTo(axe, brigand);
	}
}
