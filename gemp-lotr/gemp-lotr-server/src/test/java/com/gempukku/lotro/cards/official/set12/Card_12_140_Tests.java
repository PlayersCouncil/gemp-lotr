package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.VitalityModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_140_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("crush", "12_140");
					// A second copy: the cost names "this minion".
					put("crush2", "12_140");

					// Murderous Uruk: an [uruk-hai] Uruk-hai, strength 8, damage +1 and muster (a regroup matter) only.
					put("uruk", "11_195");
					// Saruman, Agent of the Dark Lord: an [uruk-hai] minion who is no Uruk-hai (a Wizard), strength 8, a
					// lurker.  His skirmish ability is never used, and no companion is killed for his heal.
					put("saruman", "12_144");
					// Cirith Ungol Guard: a [sauron] Uruk-hai, strength 9, damage +1 and no other text.
					put("cuguard", "10_81");

					// Gimli, Son of Gloin: a Dwarf companion, strength 6, vitality 3.  His skirmish ability is never used.
					put("gimli", "1_13");
					// Dwarven Armor: a [dwarven] armor possession; its bearer is overwhelmed only at triple strength.
					put("armor", "1_8");
					put("armor2", "1_8");
					// Hand Axe: a [dwarven] hand weapon possession, strength +1; its archery ability is never used.
					put("handaxe", "2_10");
					// Ring of Artifice: a [dwarven] ARTIFACT a Dwarf bears (no strength with no [dwarven] artifact in the
					// discard pile); its regroup ability is never used.
					put("ring", "17_4");
					// Spider Poison: a Shadow possession a companion can bear; its bearer exerts as he is assigned.
					put("poison", "8_28");
					// Dwarf Guard: another companion, who bears Dwarven Axe and fights nobody.
					put("guard", "1_7");
					// Dwarven Axe: a [dwarven] hand weapon possession; its trigger answers a minion losing to its bearer,
					// which no test reaches.
					put("dwaxe", "1_9");
					// Old Toby: a Free Peoples possession that nobody bears, in the support area.
					put("toby", "1_305");
					// Aragorn, Ranger of the North, strength 8, with Ranger's Sword (strength +2, damage +1).
					put("aragorn", "1_89");
					put("sword", "1_112");
					// Thrarin, Dwarven Smith: a Dwarf ally; his maneuver ability lets him skirmish.
					put("thrarin", "1_27");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void CrushingUrukStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Crushing Uruk
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 10
		 * Vitality: 3
		 * Site Number: 5
		 * Game Text: <b>Lurker</b>. <helper>(Skirmishes involving lurker minions must be resolved after any others.)</helper><br><b>Response:</b> If a companion loses a skirmish involving an [uruk-hai] minion, exert this minion to discard a possession borne by that companion.
		*/

		var scn = GetScenario();

		var crush = scn.GetFreepsCard("crush");

		assertEquals("Crushing Uruk", crush.getBlueprint().getTitle());
		assertNull(crush.getBlueprint().getSubtitle());
		assertFalse(crush.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, crush.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, crush.getBlueprint().getCulture());
		assertEquals(CardType.MINION, crush.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, crush.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(crush, Keyword.LURKER));
		assertEquals(4, crush.getBlueprint().getTwilightCost());
		assertEquals(10, crush.getBlueprint().getStrength());
		assertEquals(3, crush.getBlueprint().getVitality());
		assertEquals(5, crush.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhenACompanionLosesToAnUrukHaiItMayExertToDiscardAPossessionThatCompanionBears() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var handaxe = scn.GetFreepsCard("handaxe");
		var ring = scn.GetFreepsCard("ring");
		var guard = scn.GetFreepsCard("guard");
		var dwaxe = scn.GetFreepsCard("dwaxe");
		var toby = scn.GetFreepsCard("toby");
		scn.MoveCompanionsToTable(gimli, guard);
		scn.AttachCardsTo(gimli, armor, handaxe, ring);
		scn.AttachCardsTo(guard, dwaxe);
		scn.MoveCardsToSupportArea(toby);

		var crush = scn.GetShadowCard("crush");
		var crush2 = scn.GetShadowCard("crush2");
		var poison = scn.GetShadowCard("poison");

		scn.StartGame();
		scn.MoveMinionsToTable(crush, crush2);
		scn.AttachCardsTo(gimli, poison);

		assertAttachedTo(armor, gimli);
		assertAttachedTo(handaxe, gimli);
		assertAttachedTo(ring, gimli);
		assertAttachedTo(poison, gimli);
		assertAttachedTo(dwaxe, guard);
		assertInZone(Zone.SUPPORT, toby);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, crush);
		// The second copy fights nobody.
		scn.ShadowDeclineAssignments();
		// Spider Poison: Gimli exerts as he is assigned.
		assertEquals(1, scn.GetWoundsOn(gimli));
		scn.FreepsResolveSkirmish(gimli);

		// 10 against Gimli's 7: he loses and takes one wound.
		assertEquals(7, scn.GetStrength(gimli));
		assertEquals(0, scn.GetWoundsOn(crush));
		assertEquals(0, scn.GetWoundsOn(crush2));
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(gimli));

		// Each copy offers, the one that fights nobody as well; the copy taken exerts, and not the other.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(crush));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(crush2));
		scn.ShadowChooseOptionalTrigger(crush2);
		assertEquals(1, scn.GetWoundsOn(crush2));
		assertEquals(0, scn.GetWoundsOn(crush));

		// The loser's possessions, of either side; not his artifact, another companion's possession, or one nobody bears.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(armor, handaxe, poison));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ring, dwaxe, toby));
		scn.ShadowChooseCard(handaxe);

		assertInDiscard(handaxe);
		assertAttachedTo(armor, gimli);
		assertAttachedTo(ring, gimli);
		assertAttachedTo(poison, gimli);
		assertAttachedTo(dwaxe, guard);
		assertInZone(Zone.SUPPORT, toby);

		// A response with a cost may be used again for the same loss while the cost can be paid: declined here.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(crush));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(crush2));
		assertEquals(0, scn.GetWoundsOn(crush));
		assertAttachedTo(armor, gimli);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ALossToAnUrukHaiWizardIsAnsweredToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, armor);

		var crush = scn.GetShadowCard("crush");
		var saruman = scn.GetShadowCard("saruman");

		scn.StartGame();
		scn.MoveMinionsToTable(crush, saruman);
		assertAttachedTo(armor, gimli);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, saruman);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);

		// Saruman's 8 beats Gimli's 6.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(crush));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(crush));
		scn.ShadowAcceptOptionalTrigger();

		// The armor is Gimli's one possession: discarded with nothing asked.
		assertEquals(1, scn.GetWoundsOn(crush));
		assertInDiscard(armor);
		scn.ShadowDeclineOptionalTrigger();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NothingIsOfferedForACompanionWhoWinsForALossToASauronUrukHaiOrForAnAllyWhoLoses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var thrarin = scn.GetFreepsCard("thrarin");
		var armor2 = scn.GetFreepsCard("armor2");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(thrarin);
		scn.AttachCardsTo(aragorn, sword);
		scn.AttachCardsTo(gimli, armor);
		scn.AttachCardsTo(thrarin, armor2);

		var crush = scn.GetShadowCard("crush");
		var uruk = scn.GetShadowCard("uruk");
		var cuguard = scn.GetShadowCard("cuguard");
		var saruman = scn.GetShadowCard("saruman");

		scn.StartGame();
		// Vitality raised by hand only so that Thrarin lives through his exertion and his loss.
		scn.ApplyAdHocModifier(new VitalityModifier(null, thrarin, null, 2));
		scn.MoveMinionsToTable(crush, uruk, cuguard, saruman);
		assertAttachedTo(sword, aragorn);
		assertAttachedTo(armor, gimli);
		assertAttachedTo(armor2, thrarin);

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(thrarin));
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, uruk},
				new PhysicalCardImpl[]{gimli, cuguard},
				new PhysicalCardImpl[]{thrarin, saruman});
		scn.ShadowDeclineAssignments();

		// Aragorn's 10 beats the Murderous Uruk's 8: a companion who wins a skirmish involving an [uruk-hai] minion.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		// The Guard's 9 beats Gimli's 6: a loss to a [sauron] Uruk-hai.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		// Saruman's 8 beats Thrarin's 4 (not tripled, so not overwhelmed): an ally who loses.
		scn.FreepsResolveSkirmish(thrarin);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(thrarin));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		assertEquals(0, scn.GetWoundsOn(crush));
		assertAttachedTo(sword, aragorn);
		assertAttachedTo(armor, gimli);
		assertAttachedTo(armor2, thrarin);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithOnlyAnArtifactBorneItIsOfferedAndExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var ring = scn.GetFreepsCard("ring");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, ring);

		var crush = scn.GetShadowCard("crush");

		scn.StartGame();
		scn.MoveMinionsToTable(crush);
		assertAttachedTo(ring, gimli);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, crush);
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(crush));

		// The response has a cost, so it is offered though Gimli bears no possession.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(crush));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(crush));
		assertAttachedTo(ring, gimli);
		scn.ShadowDeclineOptionalTrigger();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnExhaustedCrushingUrukIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, armor);

		var crush = scn.GetShadowCard("crush");

		scn.StartGame();
		scn.MoveMinionsToTable(crush);
		// Vitality 3 with 2 wounds: it cannot exert.
		scn.AddWoundsToChar(crush, 2);
		assertEquals(2, scn.GetWoundsOn(crush));
		assertAttachedTo(armor, gimli);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, crush);
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetWoundsOn(crush));
		assertAttachedTo(armor, gimli);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
