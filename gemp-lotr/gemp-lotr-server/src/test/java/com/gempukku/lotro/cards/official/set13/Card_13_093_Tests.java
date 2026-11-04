package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_093_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("harmless", "13_93");
					put("harmless2", "13_93");

					// Axeman of Harad: a [men] Man, strength 11, vitality 3.  Its text only lowers its own cost in hand.
					put("axeman", "11_69");
					// Saruman, Coldly Still: a [men] minion who is no Man (Wizard), strength 8, vitality 4.  His +2 is
					// for [men] minions bearing a possession, and none does; his skirmish ability is never used.
					put("saruman", "17_48");
					// Footman of Dunland: a [men] Man, strength 7, vitality 3.  Its text makes the current site a plains,
					// which nothing here reads.
					put("footman", "11_82");
					// Swarthy Hillman: a [men] Man, strength 7, vitality 1.  An archer only while its player controls a
					// site, and none is controlled.
					put("swarthy", "15_93");
					// Dunlending Brigand: a [dunland] Man minion, strength 8, vitality 1, no text.
					put("brigand", "4_10");
					// Morgul Slayer: [sauron] Orc, strength 7, vitality 2.  A regroup ability, never used.
					put("slayer", "3_93");
					// Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1 and no other text.
					put("savage", "1_151");

					// Aragorn, Ranger of the North: a [gondor] Man, strength 8, vitality 4.  A maneuver ability, never used.
					put("aragorn", "1_89");
					// Gimli, Son of Gloin: a [dwarven] Dwarf, strength 6, vitality 3, damage +1.  A skirmish ability,
					// never used.
					put("gimli", "1_13");
					// Thrarin, Dwarven Smith: a Dwarf ally, strength 4, vitality 2.  His maneuver ability lets him take
					// part in skirmishes until the regroup phase.
					put("thrarin", "1_27");
					// Éowyn, Lady of Ithilien: [rohan] Man, strength 6, vitality 3.  Skirmish: exert Éowyn to choose an
					// opponent, who must wound a minion for each wound on each minion skirmishing Éowyn.
					put("eowyn", "10_72");
					// Defend It and Hope: [gondor] Maneuver event.  Exert a [gondor] Man to wound a minion once (twice
					// if an Uruk-hai).
					put("defend", "4_115");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HarmlessStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Harmless
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Resistance: -1
		 * Game Text: <b>Response:</b> If a companion loses a skirmish involving a [men] minion, transfer this condition from your support area to that companion. Limit 1 per companion.<br>Minions cannot take wounds during skirmishes involving bearer.
		*/

		var scn = GetScenario();

		var harmless = scn.GetFreepsCard("harmless");

		assertEquals("Harmless", harmless.getBlueprint().getTitle());
		assertNull(harmless.getBlueprint().getSubtitle());
		assertFalse(harmless.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, harmless.getBlueprint().getSide());
		assertEquals(Culture.MEN, harmless.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, harmless.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(harmless, Keyword.SUPPORT_AREA));
		assertEquals(2, harmless.getBlueprint().getTwilightCost());
		assertEquals(-1, harmless.getBlueprint().getResistance());
	}

	@Test
	public void ACompanionWhoLosesToAMenManTakesOneCopyAndAnotherCompanionWhoLosesToAMenWizardTakesTheOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var harmless = scn.GetShadowCard("harmless");
		var harmless2 = scn.GetShadowCard("harmless2");
		var axeman = scn.GetShadowCard("axeman");
		var saruman = scn.GetShadowCard("saruman");

		scn.StartGame();
		scn.MoveCardsToSupportArea(harmless, harmless2);
		assertInZone(Zone.SUPPORT, harmless);
		assertInZone(Zone.SUPPORT, harmless2);
		scn.MoveMinionsToTable(axeman, saruman);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, axeman},
				new PhysicalCardImpl[]{gimli, saruman});

		// The Axeman's 11 beats Aragorn's 8.  Each copy makes its own offer.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(harmless));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(harmless2));
		scn.ShadowChooseOptionalTrigger(harmless);
		assertAttachedTo(harmless, aragorn);
		assertInZone(Zone.SUPPORT, harmless2);

		// Limit 1 per companion: the second copy is not offered for the same loss, and the game goes on to the next
		// skirmish.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(harmless2));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// Saruman, a [men] Wizard, beats Gimli: the second copy goes to him.  The first, borne by Aragorn, is no
		// longer in the support area and is not moved on.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(harmless));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(harmless2));
		scn.ShadowAcceptOptionalTrigger();
		assertAttachedTo(harmless2, gimli);
		assertAttachedTo(harmless, aragorn);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NothingIsOfferedForACompanionWhoWinsForALossToAMinionOfAnotherCultureOrForAnAllyWhoLoses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(thrarin);

		var harmless = scn.GetShadowCard("harmless");
		var swarthy = scn.GetShadowCard("swarthy");
		var brigand = scn.GetShadowCard("brigand");
		var axeman = scn.GetShadowCard("axeman");

		scn.StartGame();
		scn.MoveCardsToSupportArea(harmless);
		assertInZone(Zone.SUPPORT, harmless);
		scn.MoveMinionsToTable(swarthy, brigand, axeman);
		scn.SkipToPhase(Phase.MANEUVER);
		// Thrarin exerts to take part in skirmishes.
		assertEquals(0, scn.GetWoundsOn(thrarin));
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, swarthy},
				new PhysicalCardImpl[]{gimli, brigand},
				new PhysicalCardImpl[]{thrarin, axeman});
		scn.ShadowDeclineAssignments();

		// Aragorn's 8 beats the Swarthy Hillman's 7, a [men] minion: a companion who wins.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInDiscard(swarthy);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(harmless));

		// The Brigand's 8 beats Gimli's 6: a loss to a [dunland] Man.
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(harmless));

		// The Axeman's 11 beats Thrarin's 4, a [men] minion's win over an ally: he is overwhelmed.
		scn.FreepsResolveSkirmish(thrarin);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, thrarin);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(harmless));
		assertInZone(Zone.SUPPORT, harmless);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void MinionsLosingToBearerTakeNoWoundsWhileOthersAndWoundsOutsideSkirmishesLand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(defend);

		var harmless = scn.GetShadowCard("harmless");
		var slayer = scn.GetShadowCard("slayer");
		var footman = scn.GetShadowCard("footman");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.AttachCardsTo(aragorn, harmless);
		assertAttachedTo(harmless, aragorn);
		// A strength that only arranges the board: Aragorn beats two minions at once.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 10));
		scn.MoveMinionsToTable(slayer, footman, savage);
		scn.SkipToPhase(Phase.MANEUVER);

		// In the maneuver phase there is no skirmish: Aragorn exerts to wound the Footman, and the wound lands.
		assertEquals(0, scn.GetWoundsOn(footman));
		scn.FreepsPlayCard(defend);
		scn.FreepsChooseCard(footman);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(footman));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, slayer},
				new PhysicalCardImpl[]{gimli, savage});
		// The Footman is left over, and the Shadow player puts it with Aragorn.
		scn.ShadowAssignToMinions(aragorn, footman);

		// Gimli's 6 beats the Savage's 5 in a skirmish without the bearer: two wounds (damage +1).
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(savage));

		// Aragorn's 18 beats the [sauron] Orc and the [men] Man together: neither takes a wound.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(slayer));
		assertEquals(1, scn.GetWoundsOn(footman));
		scn.PassSkirmishActions();
		assertEquals(0, scn.GetWoundsOn(slayer));
		assertEquals(1, scn.GetWoundsOn(footman));
		assertInZone(Zone.SHADOW_CHARACTERS, slayer);
		assertInZone(Zone.SHADOW_CHARACTERS, footman);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	// Q-0387: A (Ketura, 2026-10-28): during the bearer's skirmish no minion anywhere can take a wound.
	@Test
	public void DuringBearersSkirmishAMinionInNoSkirmishTakesNoWoundEither() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(eowyn, gimli);

		var harmless = scn.GetShadowCard("harmless");
		var footman = scn.GetShadowCard("footman");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.AttachCardsTo(eowyn, harmless);
		assertAttachedTo(harmless, eowyn);
		scn.MoveMinionsToTable(footman, savage);
		// One wound on the Footman: Éowyn's ability will have the Shadow player wound one minion.
		scn.AddWoundsToChar(footman, 1);
		assertEquals(1, scn.GetWoundsOn(footman));
		assertEquals(0, scn.GetWoundsOn(savage));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{eowyn, footman},
				new PhysicalCardImpl[]{gimli, savage});

		// Éowyn's skirmish first; Gimli and the Savage wait for theirs.
		scn.FreepsResolveSkirmish(eowyn);
		assertTrue(scn.IsCharSkirmishing(eowyn));
		assertFalse(scn.IsCharSkirmishing(savage));
		scn.FreepsUseCardAction(eowyn);
		assertEquals(1, scn.GetWoundsOn(eowyn));

		// "Minions cannot take wounds during skirmishes involving bearer": no minion takes one while hers goes on.
		assertEquals(1, scn.GetWoundsOn(footman));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
