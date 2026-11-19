package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_192_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sword", "11_192");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8;
					                            // a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf companion, strength 6; a skirmish
					                            // ability used only on purpose
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally, strength 2, home site 2; a skirmish
					                            // ability used only on purpose

					put("swarming", "11_203");  // Swarming Uruk: an [uruk-hai] Uruk-hai, strength 9; cannot take wounds
					                            // while another [uruk-hai] minion is assigned, which none is here
					put("saruman", "17_116");   // Saruman, Master of the White Hand: an [uruk-hai] minion who is a Wizard;
					                            // each companion resistance -1, a skirmish ability used only on purpose
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai, strength 5; damage +1 and nothing else
					put("alliance", "12_141");  // Dark Alliance: an [uruk-hai] condition; a response used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IsengardSwordStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Isengard Sword
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Game Text: Bearer must be an [uruk-hai] minion.<br>Each character skirmishing bearer is strength -2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("sword");

		assertEquals("Isengard Sword", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsPlayedOnAnUrukHaiCultureMinionOfAnyRaceAndNotOnAnotherCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetShadowCard("sword");
		var swarming = scn.GetShadowCard("swarming");
		var saruman = scn.GetShadowCard("saruman");
		var savage = scn.GetShadowCard("savage");
		var alliance = scn.GetShadowCard("alliance");
		scn.MoveCardsToHand(sword);
		scn.MoveCardsToSupportArea(alliance);

		scn.StartGame();
		assertInHand(sword);
		scn.MoveMinionsToTable(swarming, saruman, savage);
		scn.FreepsPass();

		assertInHand(sword);
		assertTrue(scn.ShadowPlayAvailable(sword));
		scn.ShadowPlayCard(sword);

		// An [uruk-hai] Uruk-hai and an [uruk-hai] Wizard; not an [isengard] Uruk-hai, nor an [uruk-hai] condition.
		assertTrue(scn.ShadowHasCardChoiceAvailable(swarming, saruman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage, alliance, scn.GetRingBearer()));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(swarming);

		assertAttachedTo(sword, swarming);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheCompanionSkirmishingBearerIsStrengthMinus2AndNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetShadowCard("sword");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var swarming = scn.GetShadowCard("swarming");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(swarming, savage);
		scn.AttachCardsTo(swarming, sword);
		assertAttachedTo(sword, swarming);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, savage}, new PhysicalCardImpl[]{aragorn, swarming});
		scn.FreepsResolveSkirmish(gimli);

		// Gimli's skirmish: he is skirmishing another minion, and Aragorn, assigned to the bearer, is not
		// skirmishing yet.
		assertTrue(scn.IsCharSkirmishingAgainst(gimli, savage));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(9, scn.GetStrength(swarming));
		assertEquals(4, scn.GetStrength(frodo));    // Frodo 3 + 1 for The Ruling Ring; in no skirmish
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(aragorn);

		// Aragorn's skirmish: he is 8 - 2; the bearer itself, Gimli and Frodo are as printed.
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, swarming));
		assertEquals(6, scn.GetStrength(aragorn));
		assertEquals(9, scn.GetStrength(swarming));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(4, scn.GetStrength(frodo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AnAllySkirmishingBearerIsStrengthMinus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetShadowCard("sword");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveAlliesToTable(bounder);

		var swarming = scn.GetShadowCard("swarming");

		scn.StartGame();
		scn.MoveMinionsToTable(swarming);
		scn.AttachCardsTo(swarming, sword);
		assertAttachedTo(sword, swarming);

		scn.SkipToAssignments();
		// Site 2 is Bounder's home: he may be assigned there.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetStrength(bounder));
		scn.FreepsAssignToMinions(bounder, swarming);
		scn.FreepsResolveSkirmish(bounder);

		assertTrue(scn.IsCharSkirmishingAgainst(bounder, swarming));
		assertEquals(0, scn.GetStrength(bounder));   // 2 - 2
		assertEquals(9, scn.GetStrength(swarming));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
