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

public class Card_11_109_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gauntlets", "11_109");
					put("gauntlets2", "11_109");

					put("miscreant", "11_131");  // Orc Miscreant: [orc] Orc, strength 7, vitality 3; a regroup ability used only on purpose
					put("halftroll", "14_12");   // Half-troll of Far Harad: [orc] Half-troll, strength 8, vitality 2; its assignment trigger wants more companions than minions
					put("denizen", "11_115");    // Denizen of Khazad-dum: [orc] Orc, strength 9, vitality 2; +2 only at an underground site
					put("troop", "1_177");       // Goblin Patrol Troop: a [moria] Orc, strength 13, no text
					put("beckoning", "12_83");   // The Beckoning Shadow: an [orc] condition, cheated into the support area

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("boromir", "1_96");      // Boromir, Lord of Gondor: strength 7, vitality 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BladedGauntletsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Bladed Gauntlets
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Armor
		 * Game Text: Bearer must be an [orc] minion.<br>Bearer is a <b>lurker</b>. <helper>(Skirmishes involving lurker minions must be resolved after any others.)</helper><br><b>Skirmish:</b> Exert bearer to make an [orc] minion strength +2.
		*/

		var scn = GetScenario();

		var gauntlets = scn.GetFreepsCard("gauntlets");

		assertEquals("Bladed Gauntlets", gauntlets.getBlueprint().getTitle());
		assertNull(gauntlets.getBlueprint().getSubtitle());
		assertFalse(gauntlets.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, gauntlets.getBlueprint().getSide());
		assertEquals(Culture.ORC, gauntlets.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, gauntlets.getBlueprint().getCardType());
		assertTrue(gauntlets.getBlueprint().getPossessionClasses().contains(PossessionClass.ARMOR));
		assertEquals(1, gauntlets.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnAnOrcMinionAndMakesHimALurker() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gauntlets = scn.GetShadowCard("gauntlets");
		var miscreant = scn.GetShadowCard("miscreant");
		var halftroll = scn.GetShadowCard("halftroll");
		var troop = scn.GetShadowCard("troop");
		var beckoning = scn.GetShadowCard("beckoning");
		scn.MoveCardsToHand(gauntlets);
		assertInHand(gauntlets);

		scn.StartGame();
		scn.MoveMinionsToTable(miscreant, halftroll, troop);
		scn.MoveCardsToSupportArea(beckoning);
		scn.SetTwilight(10);
		assertInZone(Zone.SUPPORT, beckoning);
		scn.FreepsPass();

		assertFalse(scn.HasKeyword(halftroll, Keyword.LURKER));
		assertFalse(scn.HasKeyword(miscreant, Keyword.LURKER));
		assertTrue(scn.ShadowPlayAvailable(gauntlets));

		scn.ShadowPlayCard(gauntlets);
		// An [orc] Orc and an [orc] Half-troll are offered; a [moria] Orc and an [orc] condition are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(miscreant, halftroll));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop, beckoning));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(halftroll);

		assertAttachedTo(gauntlets, halftroll);
		// The bearer is a lurker; the [orc] Orc beside him, who does not bear it, is not.
		assertTrue(scn.HasKeyword(halftroll, Keyword.LURKER));
		assertFalse(scn.HasKeyword(miscreant, Keyword.LURKER));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SkirmishAbilityExertsBearerToMakeAnOrcMinionStrengthPlusTwoUntilThatSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gauntlets = scn.GetShadowCard("gauntlets");
		var gauntlets2 = scn.GetShadowCard("gauntlets2");
		var miscreant = scn.GetShadowCard("miscreant");
		var halftroll = scn.GetShadowCard("halftroll");
		var denizen = scn.GetShadowCard("denizen");
		var troop = scn.GetShadowCard("troop");
		var beckoning = scn.GetShadowCard("beckoning");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		scn.StartGame();
		scn.MoveMinionsToTable(miscreant, halftroll, denizen, troop);
		scn.MoveCardsToSupportArea(beckoning);
		// The Half-troll, a bearer who is no Orc, bears the copy used; the Denizen bears the other copy.
		scn.AttachCardsTo(halftroll, gauntlets);
		scn.AttachCardsTo(denizen, gauntlets2);
		assertAttachedTo(gauntlets, halftroll);
		assertAttachedTo(gauntlets2, denizen);
		assertInZone(Zone.SUPPORT, beckoning);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{boromir, miscreant}, new PhysicalCardImpl[]{aragorn, halftroll});
		// The Shadow player puts the Troop on Boromir too and leaves the Denizen unassigned.
		scn.ShadowAssignToMinions(boromir, troop);

		// The Half-troll is a lurker: Boromir's skirmish comes first, and the Half-troll waits for his own.
		scn.FreepsResolveSkirmish(boromir);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(halftroll));
		assertEquals(0, scn.GetWoundsOn(denizen));
		assertEquals(0, scn.GetWoundsOn(miscreant));
		assertEquals(8, scn.GetStrength(halftroll));
		assertEquals(7, scn.GetStrength(miscreant));
		assertEquals(9, scn.GetStrength(denizen));
		assertTrue(scn.ShadowActionAvailable(gauntlets));

		scn.ShadowUseCardAction(gauntlets);
		// Its own bearer exerts; the other copy's bearer does not.
		assertEquals(1, scn.GetWoundsOn(halftroll));
		assertEquals(0, scn.GetWoundsOn(denizen));
		assertEquals(0, scn.GetWoundsOn(miscreant));
		// An [orc] minion in this skirmish, one waiting for his own and one assigned to none are offered;
		// the [moria] Orc in this skirmish and the [orc] condition are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(miscreant, halftroll, denizen));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop, beckoning));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(halftroll);

		assertEquals(10, scn.GetStrength(halftroll));
		assertEquals(7, scn.GetStrength(miscreant));
		assertEquals(9, scn.GetStrength(denizen));
		assertEquals(13, scn.GetStrength(troop));

		// Still +2 at the next decision of this skirmish.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(10, scn.GetStrength(halftroll));

		scn.PassSkirmishActions();

		// Gone in the next skirmish: each skirmish is a phase of its own.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(8, scn.GetStrength(halftroll));
		assertEquals(1, scn.GetWoundsOn(halftroll));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWhileItsBearerIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gauntlets = scn.GetShadowCard("gauntlets");
		var miscreant = scn.GetShadowCard("miscreant");
		var halftroll = scn.GetShadowCard("halftroll");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(miscreant, halftroll);
		scn.AttachCardsTo(miscreant, gauntlets);
		// Vitality 3 with 2 wounds: exhausted.  The Half-troll beside him could exert.
		scn.AddWoundsToChar(miscreant, 2);
		assertAttachedTo(gauntlets, miscreant);
		assertEquals(2, scn.GetWoundsOn(miscreant));
		assertEquals(0, scn.GetWoundsOn(halftroll));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, miscreant);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(gauntlets));
		assertEquals(2, scn.GetWoundsOn(miscreant));
		assertEquals(0, scn.GetWoundsOn(halftroll));
	}
}
