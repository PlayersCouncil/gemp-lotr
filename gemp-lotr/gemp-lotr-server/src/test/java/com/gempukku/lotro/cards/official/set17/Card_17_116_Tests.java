package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_116_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("saruman", "17_116");
					put("staff", "4_174");       // Saruman's Staff: "Maneuver: Make the first sentence of Saruman's game text not apply until the regroup phase"

					put("feral", "11_183");      // Feral Uruk: [uruk-hai] Uruk-hai, strength 9, vitality 2; its text only at a battleground, and this path has none
					put("raiders", "1_158");     // Uruk-hai Raiding Party: an [isengard] Uruk-hai, strength 9, damage +1 and nothing else
					put("foes", "11_196");       // Our Foes Are Weak: an [uruk-hai] condition borne by an unbound companion; strength -1, resistance -3

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: strength 8, vitality 4, resistance 6; his ability used only on purpose
					put("gimli", "1_13");        // Gimli, Son of Gloin: strength 6, vitality 3, resistance 6; his ability used only on purpose
					put("nostranger", "1_108");  // No Stranger to the Shadows: a Free Peoples condition on a ranger; each site's Shadow number -1
					put("sword", "1_112");       // Ranger's Sword: a possession Aragorn bears; strength +2, damage +1
					put("heart", "1_10");        // Dwarven Heart: a Free Peoples condition on a Dwarf; its texts answer its play and the start of its owner's turn
					put("mallorn", "1_54");      // Mallorn-trees: a Free Peoples condition in the support area; its ability used only on purpose
					put("thrarin", "1_27");      // Thrarin: a Dwarf ally whose maneuver ability lets him take part in skirmishes
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SarumanStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Saruman, Master of the White Hand
		 * Unique: True
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Wizard
		 * Strength: 8
		 * Vitality: 4
		 * Site Number: 4
		 * Game Text: Each companion is resistance -1.
		 * <b>Skirmish:</b> Exert Saruman to discard a condition borne by a companion skirmishing an [uruk-hai] minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("saruman");

		assertEquals("Saruman", card.getBlueprint().getTitle());
		assertEquals("Master of the White Hand", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.WIZARD, card.getBlueprint().getRace());
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhileSarumanIsInPlayEachCompanionIsResistanceMinus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(saruman);
		assertInHand(saruman);

		scn.StartGame();
		// In his owner's hand he does nothing.
		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(6, scn.GetResistance(aragorn));
		assertEquals(6, scn.GetResistance(gimli));

		// Laid in the fellowship phase, where he stands later in the turn.
		scn.MoveMinionsToTable(saruman);
		scn.FreepsRefreshDecision();
		assertEquals(9, scn.GetResistance(frodo));
		assertEquals(5, scn.GetResistance(aragorn));
		assertEquals(5, scn.GetResistance(gimli));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityExertsSarumanToDiscardAConditionBorneByACompanionHeIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		var feral = scn.GetShadowCard("feral");
		var foes = scn.GetShadowCard("foes");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var nostranger = scn.GetFreepsCard("nostranger");
		var sword = scn.GetFreepsCard("sword");
		var heart = scn.GetFreepsCard("heart");
		var mallorn = scn.GetFreepsCard("mallorn");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(aragorn, nostranger, sword);
		scn.MoveCardsToSupportArea(mallorn);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, feral);
		scn.AttachCardsTo(aragorn, foes);
		scn.AttachCardsTo(gimli, heart);
		assertAttachedTo(nostranger, aragorn);
		assertAttachedTo(sword, aragorn);
		assertAttachedTo(foes, aragorn);
		assertAttachedTo(heart, gimli);
		assertInZone(Zone.SUPPORT, mallorn);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, saruman}, new PhysicalCardImpl[]{gimli, feral});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(saruman));
		assertTrue(scn.ShadowActionAvailable(saruman));
		scn.ShadowUseCardAction(saruman);
		assertEquals(1, scn.GetWoundsOn(saruman));

		// A condition of either side borne by the companion skirmishing an [uruk-hai] minion, Saruman himself; not
		// his possession, nor a condition of a companion whose skirmish is still to come, nor one in a support area.
		assertTrue(scn.ShadowHasCardChoiceAvailable(nostranger, foes));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sword, heart, mallorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(nostranger);

		assertInDiscard(nostranger);
		assertAttachedTo(foes, aragorn);
		assertAttachedTo(sword, aragorn);
		assertAttachedTo(heart, gimli);
		assertInZone(Zone.SUPPORT, mallorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void InASkirmishOfAnotherCultureNothingIsDiscardedAndInAnUrukHaisItIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		var feral = scn.GetShadowCard("feral");
		var raiders = scn.GetShadowCard("raiders");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var nostranger = scn.GetFreepsCard("nostranger");
		var heart = scn.GetFreepsCard("heart");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(aragorn, nostranger);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, feral, raiders);
		scn.AttachCardsTo(gimli, heart);
		assertAttachedTo(nostranger, aragorn);
		assertAttachedTo(heart, gimli);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, raiders}, new PhysicalCardImpl[]{aragorn, feral});
		scn.ShadowDeclineAssignments();

		// Gimli skirmishes the [isengard] Uruk-hai: the ability is offered and paid for, and discards nothing.
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(saruman));
		scn.ShadowUseCardAction(saruman);
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertAttachedTo(heart, gimli);
		assertAttachedTo(nostranger, aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// 9 against 6, with damage +1: Gimli takes two wounds and lives.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(gimli));

		// Aragorn skirmishes the [uruk-hai] Uruk-hai: his condition is discarded.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		scn.ShadowUseCardAction(saruman);
		assertEquals(2, scn.GetWoundsOn(saruman));
		assertInDiscard(nostranger);
		assertAttachedTo(heart, gimli);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AConditionBorneByAnAllySkirmishingAnUrukHaiMinionIsNotDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		var feral = scn.GetShadowCard("feral");
		var thrarin = scn.GetFreepsCard("thrarin");
		var heart = scn.GetFreepsCard("heart");
		scn.MoveAlliesToTable(thrarin);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, feral);
		scn.AttachCardsTo(thrarin, heart);
		assertAttachedTo(heart, thrarin);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, feral);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(thrarin);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(saruman));
		scn.ShadowUseCardAction(saruman);
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertAttachedTo(heart, thrarin);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWhileSarumanIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		var feral = scn.GetShadowCard("feral");
		var aragorn = scn.GetFreepsCard("aragorn");
		var nostranger = scn.GetFreepsCard("nostranger");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, nostranger);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, feral);
		// Vitality 4 with 3 wounds: exhausted.
		scn.AddWoundsToChar(saruman, 3);
		assertEquals(3, scn.GetWoundsOn(saruman));
		assertAttachedTo(nostranger, aragorn);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, feral);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(saruman));
		assertAttachedTo(nostranger, aragorn);
	}

	@Test
	public void WithHisFirstSentenceMadeNotToApplyBySarumansStaffCompanionsHaveTheirResistanceAndHisSkirmishAbilityStillWorks() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var saruman = scn.GetShadowCard("saruman");
		var staff = scn.GetShadowCard("staff");
		var feral = scn.GetShadowCard("feral");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var nostranger = scn.GetFreepsCard("nostranger");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, nostranger);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, feral);
		scn.AttachCardsTo(saruman, staff);
		assertAttachedTo(staff, saruman);
		assertAttachedTo(nostranger, aragorn);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(9, scn.GetResistance(frodo));
		assertEquals(5, scn.GetResistance(aragorn));
		scn.FreepsPass();
		scn.ShadowUseCardAction(staff);
		assertEquals(10, scn.GetResistance(frodo));
		assertEquals(6, scn.GetResistance(aragorn));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, feral);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// His second sentence still works.
		assertTrue(scn.ShadowActionAvailable(saruman));
		scn.ShadowUseCardAction(saruman);
		assertEquals(1, scn.GetWoundsOn(saruman));
		assertInDiscard(nostranger);
		assertEquals(6, scn.GetResistance(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
