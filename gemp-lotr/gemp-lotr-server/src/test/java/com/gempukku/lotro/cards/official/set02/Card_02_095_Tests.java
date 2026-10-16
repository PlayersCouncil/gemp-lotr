package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_095_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("blade1", "2_95");
					put("blade2", "2_95");
					put("soldier1", "1_271");   // Orc Soldier: [sauron] Orc, strength 7; a skirmish ability only
					put("soldier2", "1_271");
					put("band", "1_270");       // Orc Scouting Band: [sauron] Orc, strength 8; a roaming discount and a skirmish ability
					put("guard", "10_81");      // Cirith Ungol Guard: [sauron] Uruk-hai, no Orc; damage +1 only
					put("hound", "7_192");      // Morgul Hound: [wraith] Orc; stronger only at 2 wounds on the Ring-bearer

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man; a maneuver ability only
					put("gamling", "5_82");     // Gamling, Warrior of Rohan: [rohan] Man; his skirmish ability wants a [rohan] possession
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf, no archer; stronger only against a Nazgûl
					put("gimli", "1_13");       // Gimli, Son of Glóin: a Dwarf, neither Man nor Elf; a skirmish ability only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Allies whose home is site 3 of the Fellowship sites skirmish there.
	protected VirtualTableScenario GetAllyScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("blade1", "2_95");
					put("blade2", "2_95");
					put("soldier1", "1_271");   // Orc Soldier: [sauron] Orc, strength 7; a skirmish ability only
					put("soldier2", "1_271");

					put("ottar", "1_80");       // Ottar, Man of Laketown: [gandalf] Man ally, home 3F; a fellowship ability only
					// Elrond, Lord of Rivendell: Elf ally, home 3F; he heals allies of site 3 at the start of each turn
					// and has a fellowship ability
					put("elrond", "1_40");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void VileBladeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Vile Blade
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: +2
		 * Game Text: Bearer must be a [sauron] Orc.<br>Bearer is strength +2 while skirmishing a Man or Elf.
		*/

		var scn = GetScenario();

		var blade = scn.GetFreepsCard("blade1");

		assertEquals("Vile Blade", blade.getBlueprint().getTitle());
		assertNull(blade.getBlueprint().getSubtitle());
		assertFalse(blade.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, blade.getBlueprint().getSide());
		assertEquals(Culture.SAURON, blade.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, blade.getBlueprint().getCardType());
		assertTrue(blade.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, blade.getBlueprint().getTwilightCost());
		assertEquals(2, blade.getBlueprint().getStrength());
	}

	@Test
	public void BearerMustBeASauronOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var blade = scn.GetShadowCard("blade1");
		var soldier = scn.GetShadowCard("soldier1");
		var band = scn.GetShadowCard("band");
		var guard = scn.GetShadowCard("guard");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToHand(blade);
		assertInHand(blade);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, band, guard, hound);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(blade));
		scn.ShadowPlayCard(blade);

		// Either [sauron] Orc; not the [sauron] Uruk-hai, nor the [wraith] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, band));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard, hound));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(band);

		assertAttachedTo(blade, band);
		assertNotAttachedTo(blade, soldier);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void BearerIsStrengthPlus2OnlyOnceSkirmishingAManAndNotAgainstADwarf() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var blade1 = scn.GetShadowCard("blade1");
		var blade2 = scn.GetShadowCard("blade2");
		var soldier1 = scn.GetShadowCard("soldier1");
		var soldier2 = scn.GetShadowCard("soldier2");
		var band = scn.GetShadowCard("band");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier1, soldier2, band);
		scn.AttachCardsTo(soldier1, blade1);
		scn.AttachCardsTo(soldier2, blade2);
		assertAttachedTo(blade1, soldier1);
		assertAttachedTo(blade2, soldier2);
		// 7, and the Blade's own +2.
		assertEquals(9, scn.GetStrength(soldier1));
		assertEquals(9, scn.GetStrength(soldier2));
		assertEquals(8, scn.GetStrength(band));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, soldier1}, new PhysicalCardImpl[]{gimli, soldier2});
		// The Orc Scouting Band, left over, joins the first bearer against Aragorn.
		scn.ShadowAssignToMinions(aragorn, band);

		scn.FreepsResolveSkirmish(gimli);
		// Skirmishing a Dwarf: no more than the Blade's own +2.
		assertEquals(9, scn.GetStrength(soldier2));
		// Assigned to Aragorn, whose skirmish has not begun: not yet skirmishing a Man.
		assertEquals(9, scn.GetStrength(soldier1));
		scn.FreepsPass();
		scn.ShadowPass();

		scn.FreepsResolveSkirmish(aragorn);
		// 7 + 2 + 2 against a Man.
		assertEquals(11, scn.GetStrength(soldier1));
		// The other Orc in the same skirmish bears no Blade and is unchanged.
		assertEquals(8, scn.GetStrength(band));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void BearerIsStrengthPlus2SkirmishingARohanManOrAnElfCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gamling = scn.GetFreepsCard("gamling");
		var arwen = scn.GetFreepsCard("arwen");
		var blade1 = scn.GetShadowCard("blade1");
		var blade2 = scn.GetShadowCard("blade2");
		var soldier1 = scn.GetShadowCard("soldier1");
		var soldier2 = scn.GetShadowCard("soldier2");
		scn.MoveCompanionsToTable(gamling, arwen);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier1, soldier2);
		scn.AttachCardsTo(soldier1, blade1);
		scn.AttachCardsTo(soldier2, blade2);
		assertAttachedTo(blade1, soldier1);
		assertAttachedTo(blade2, soldier2);
		assertEquals(9, scn.GetStrength(soldier1));
		assertEquals(9, scn.GetStrength(soldier2));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gamling, soldier1}, new PhysicalCardImpl[]{arwen, soldier2});

		scn.FreepsResolveSkirmish(gamling);
		assertEquals(11, scn.GetStrength(soldier1));
		scn.FreepsPass();
		scn.ShadowPass();

		scn.FreepsResolveSkirmish(arwen);
		assertEquals(11, scn.GetStrength(soldier2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void BearerIsStrengthPlus2SkirmishingAManAllyOrAnElfAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetAllyScenario();

		var ottar = scn.GetFreepsCard("ottar");
		var elrond = scn.GetFreepsCard("elrond");
		var blade1 = scn.GetShadowCard("blade1");
		var blade2 = scn.GetShadowCard("blade2");
		var soldier1 = scn.GetShadowCard("soldier1");
		var soldier2 = scn.GetShadowCard("soldier2");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveAlliesToTable(ottar, elrond);
		scn.MoveMinionsToTable(soldier1, soldier2);
		scn.AttachCardsTo(soldier1, blade1);
		scn.AttachCardsTo(soldier2, blade2);
		assertInZone(Zone.SUPPORT, ottar, elrond);
		assertAttachedTo(blade1, soldier1);
		assertAttachedTo(blade2, soldier2);
		assertEquals(9, scn.GetStrength(soldier1));
		assertEquals(9, scn.GetStrength(soldier2));

		// The fellowship moves to site 3, the allies' home.
		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{ottar, soldier1}, new PhysicalCardImpl[]{elrond, soldier2});

		scn.FreepsResolveSkirmish(ottar);
		assertEquals(11, scn.GetStrength(soldier1));
		scn.FreepsPass();
		scn.ShadowPass();

		scn.FreepsResolveSkirmish(elrond);
		assertEquals(11, scn.GetStrength(soldier2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
