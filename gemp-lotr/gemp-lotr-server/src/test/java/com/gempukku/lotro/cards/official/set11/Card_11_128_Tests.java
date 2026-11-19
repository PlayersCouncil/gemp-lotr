package com.gempukku.lotro.cards.official.set11;

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

public class Card_11_128_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("scimitar", "11_128");
					put("scimitar2", "11_128");
					put("miscreant", "11_131");   // Orc Miscreant: an [orc] Orc, vitality 3; a regroup ability used only on purpose
					put("miscreant2", "11_131");
					put("halftroll", "14_12");    // Half-troll of Far Harad: an [orc] minion who is no Orc; laid only in a Shadow phase test
					put("troop", "1_177");        // Goblin Patrol Troop: a [moria] Orc minion, no text
					put("beckoning", "12_83");    // The Beckoning Shadow: an [orc] condition; its corruption needs 5 burdens
					put("troll", "11_135");       // Porter Troll: an [orc] Troll printed fierce and damage +1, strength 15; its play trigger is never reached (cheated in)
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: [gondor] Man, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");         // Gimli, Son of Gloin: [dwarven] Dwarf; a skirmish ability used only on purpose
					put("bounder", "1_286");      // Bounder: a [shire] Hobbit ally whose home is site 2, where these skirmishes are
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MordorScimitarStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Mordor Scimitar
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: 3
		 * Game Text: Bearer must be an [orc] minion.<br>Each time bearer is assigned to skirmish a companion, you may exert that companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("scimitar");

		assertEquals("Mordor Scimitar", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnAnOrcMinionOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var scimitar = scn.GetShadowCard("scimitar");
		var miscreant = scn.GetShadowCard("miscreant");
		var halftroll = scn.GetShadowCard("halftroll");
		var troop = scn.GetShadowCard("troop");
		var beckoning = scn.GetShadowCard("beckoning");
		scn.MoveCardsToHand(scimitar);
		scn.MoveCardsToSupportArea(beckoning);

		scn.StartGame();
		scn.MoveMinionsToTable(miscreant, halftroll, troop);
		scn.SetTwilight(10);
		assertInHand(scimitar);
		assertInPlay(beckoning);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(scimitar));
		scn.ShadowPlayCard(scimitar);

		// An [orc] Orc and an [orc] Half-troll; not the [moria] Orc, nor the [orc] condition.
		assertTrue(scn.ShadowHasCardChoiceAvailable(miscreant, halftroll));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop, beckoning));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(halftroll);

		assertAttachedTo(scimitar, halftroll);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AssignedToACompanionOnlyThatBearersCopyOffersAndDeclinedNothingIsExerted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(bounder);

		var scimitar = scn.GetShadowCard("scimitar");
		var scimitar2 = scn.GetShadowCard("scimitar2");
		var miscreant = scn.GetShadowCard("miscreant");
		var miscreant2 = scn.GetShadowCard("miscreant2");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(miscreant, miscreant2, troop);
		scn.AttachCardsTo(miscreant, scimitar);
		scn.AttachCardsTo(miscreant2, scimitar2);
		scn.SkipToAssignments();
		assertAttachedTo(scimitar, miscreant);
		assertAttachedTo(scimitar2, miscreant2);
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// The bearer to a companion; the other copy's bearer to an ally; a minion with no Scimitar to a companion.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, miscreant}, new PhysicalCardImpl[]{bounder, miscreant2},
				new PhysicalCardImpl[]{gimli, troop});

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scimitar));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(scimitar2));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void TakenTheOfferExertsThatCompanionWithNoSecondQuestion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(bounder);

		var scimitar = scn.GetShadowCard("scimitar");
		var scimitar2 = scn.GetShadowCard("scimitar2");
		var miscreant = scn.GetShadowCard("miscreant");
		var miscreant2 = scn.GetShadowCard("miscreant2");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(miscreant, miscreant2, troop);
		scn.AttachCardsTo(miscreant, scimitar);
		scn.AttachCardsTo(miscreant2, scimitar2);
		scn.SkipToAssignments();
		assertAttachedTo(scimitar, miscreant);
		assertAttachedTo(scimitar2, miscreant2);
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, miscreant}, new PhysicalCardImpl[]{bounder, miscreant2},
				new PhysicalCardImpl[]{gimli, troop});

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scimitar));
		scn.ShadowAcceptOptionalTrigger();

		// "That companion": Aragorn, whom the bearer was assigned to, and nobody else.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertFalse(scn.ShadowDecisionAvailable("Would you like to exert"));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AssignedToTheRingBearerByTheShadowPlayerTheOfferExertsHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);

		var scimitar = scn.GetShadowCard("scimitar");
		var miscreant = scn.GetShadowCard("miscreant");

		scn.StartGame();
		scn.MoveMinionsToTable(miscreant);
		scn.AttachCardsTo(miscreant, scimitar);
		scn.SkipToAssignments();
		assertAttachedTo(scimitar, miscreant);
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.FreepsDeclineAssignments();
		scn.ShadowAssignToMinions(frodo, miscreant);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scimitar));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowDecisionAvailable("Would you like to exert"));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void DeclinedItIsOfferedAgainWhenTheBearerIsAssignedInTheFierceAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var scimitar = scn.GetShadowCard("scimitar");
		var troll = scn.GetShadowCard("troll");

		scn.StartGame();
		scn.MoveMinionsToTable(troll);
		scn.AttachCardsTo(troll, scimitar);
		// Aragorn 8 + 11 = 19 against the Troll's 15 + 3 = 18: he wins, and both live into the fierce round.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 11));
		scn.SkipToAssignments();
		assertAttachedTo(scimitar, troll);
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.FreepsAssignToMinions(aragorn, troll);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scimitar));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(troll));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, troll);

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(scimitar));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void NotOfferedWhenThatCompanionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.AddWoundsToChar(aragorn, 3);

		var scimitar = scn.GetShadowCard("scimitar");
		var miscreant = scn.GetShadowCard("miscreant");

		scn.StartGame();
		scn.MoveMinionsToTable(miscreant);
		scn.AttachCardsTo(miscreant, scimitar);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		scn.SkipToAssignments();
		assertAttachedTo(scimitar, miscreant);

		scn.FreepsAssignToMinions(aragorn, miscreant);

		// Vitality 4 with 3 wounds: exhausted, so the exertion would do nothing, and it is not offered.  A wound
		// in its place would kill him.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertInPlay(aragorn);
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}
}
