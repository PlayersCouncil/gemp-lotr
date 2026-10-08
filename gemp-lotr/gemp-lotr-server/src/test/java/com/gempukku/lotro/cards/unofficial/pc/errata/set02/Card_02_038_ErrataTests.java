package com.gempukku.lotro.cards.unofficial.pc.errata.set02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_038_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shield", "52_38");

					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion, strength 7, vitality 3; a skirmish ability for Hobbits
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] companion who is not Boromir; a maneuver ability only used on purpose
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] archer companion, not [gondor]; an archery ability only used on purpose
					put("merchant", "7_97");    // Gondorian Merchant: the only [gondor] ally, so [gondor] but not a companion

					put("marksman", "1_176");   // Goblin Marksman: minion archer and nothing else
					put("marksman2", "1_176");
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, strength 8, no text
					put("brigand2", "4_10");
					put("axe", "4_26");         // Iron Axe: hand weapon, strength +3, bearer must be a [dunland] Man, no other text
					put("axe2", "4_26");
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, strength 5; its when-played trigger does not fire when cheated in
					put("scimitar", "1_180");   // Goblin Scimitar: hand weapon, strength +2, bearer must be a [moria] Orc; a when-played draw only
					put("denizen", "11_116");   // Denizen of Moria: [orc] Orc, strength 7; +3 only at an underground site
					put("warg", "17_92");       // Vicious Warg: a mount, not a weapon, strength +4; makes its bearer fierce
					put("attea", "1_229");      // Ulaire Attea, Keeper of Dol Guldur: Nazgul, strength 12, fierce; a pump that needs twilight
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ShieldofBoromirStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Shield of Boromir
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Shield
		 * Game Text: Bearer must be a [gondor] companion.
		 * 	The minion archery total is -1.
		 * 	If bearer is Boromir, each minion skirmishing him does not gain strength bonuses from weapons.
		 * 	Skirmish: If bearer is Boromir, discard this to make him take no wounds.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("shield");

		assertEquals("Shield of Boromir", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.SHIELD));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnAGondorCompanionAndMakesTheMinionArcheryTotalMinus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shield = scn.GetFreepsCard("shield");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var merchant = scn.GetFreepsCard("merchant");
		scn.MoveCompanionsToTable(boromir, aragorn, legolas);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(shield);

		scn.StartGame();
		scn.MoveMinionsToTable("marksman", "marksman2");

		assertEquals(0, scn.GetTwilight());
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.FreepsPlayAvailable(shield));

		scn.FreepsPlayCard(shield);
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(legolas, merchant));
		// Not Boromir: the archery total does not depend on who bears it.
		scn.FreepsChooseCard(aragorn);

		assertAttachedTo(shield, aragorn);
		assertEquals(1, scn.GetTwilight());
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachMinionSkirmishingBoromirGainsNoStrengthFromWeapons() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shield = scn.GetFreepsCard("shield");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var brigand = scn.GetShadowCard("brigand");
		var brigand2 = scn.GetShadowCard("brigand2");
		var runner = scn.GetShadowCard("runner");
		var denizen = scn.GetShadowCard("denizen");
		scn.MoveCompanionsToTable(boromir, aragorn);
		scn.AttachCardsTo(boromir, shield);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand, brigand2, runner, denizen);
		scn.AttachCardsTo(brigand, scn.GetShadowCard("axe"));
		scn.AttachCardsTo(brigand2, scn.GetShadowCard("axe2"));
		scn.AttachCardsTo(runner, scn.GetShadowCard("scimitar"));
		scn.AttachCardsTo(denizen, scn.GetShadowCard("warg"));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		// 8 + 3 from the axe; 5 + 2 from the scimitar; 7 + 4 from the warg
		assertEquals(11, scn.GetStrength(brigand));
		assertEquals(11, scn.GetStrength(brigand2));
		assertEquals(7, scn.GetStrength(runner));
		assertEquals(11, scn.GetStrength(denizen));

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{boromir, brigand},
				new PhysicalCardImpl[]{aragorn, brigand2});
		// The Free Peoples player assigns one minion to each; the Shadow player adds the other two to Boromir.
		scn.ShadowAssignToMinions(boromir, runner, denizen);
		scn.FreepsResolveSkirmish(boromir);

		assertTrue(scn.IsCharSkirmishingAgainst(boromir, brigand));
		assertEquals(8, scn.GetStrength(brigand));
		assertEquals(5, scn.GetStrength(runner));
		// A mount is not a weapon.
		assertEquals(11, scn.GetStrength(denizen));
		// Assigned to Aragorn, whose skirmish has not begun.
		assertEquals(11, scn.GetStrength(brigand2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WhenTheBearerIsNotBoromirNoMinionLosesItsWeaponBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shield = scn.GetFreepsCard("shield");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var brigand = scn.GetShadowCard("brigand");
		var brigand2 = scn.GetShadowCard("brigand2");
		scn.MoveCompanionsToTable(boromir, aragorn);
		scn.AttachCardsTo(aragorn, shield);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand, brigand2);
		scn.AttachCardsTo(brigand, scn.GetShadowCard("axe"));
		scn.AttachCardsTo(brigand2, scn.GetShadowCard("axe2"));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, brigand},
				new PhysicalCardImpl[]{boromir, brigand2});
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, brigand));
		// 8 + 3 from the axe, against the bearer, who is not Boromir
		assertEquals(11, scn.GetStrength(brigand));
		// Assigned to Boromir, whose skirmish has not begun
		assertEquals(11, scn.GetStrength(brigand2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Aragorn loses to the Brigand's 11; then Boromir, who is skirmishing but is not the bearer.
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(boromir);

		assertTrue(scn.IsCharSkirmishingAgainst(boromir, brigand2));
		// 8 + 3 from the axe
		assertEquals(11, scn.GetStrength(brigand2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void DiscardingTheShieldMakesBoromirTakeNoWoundsForTheRestOfTheSkirmishPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shield = scn.GetFreepsCard("shield");
		var boromir = scn.GetFreepsCard("boromir");
		var attea = scn.GetShadowCard("attea");
		scn.MoveCompanionsToTable(boromir);
		scn.AttachCardsTo(boromir, shield);

		scn.StartGame();
		scn.MoveMinionsToTable(attea);
		// So that a lost skirmish is 2 wounds.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, attea, null, Keyword.DAMAGE, 1));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(boromir, attea);
		scn.FreepsResolveSkirmish(boromir);

		assertEquals(0, scn.GetWoundsOn(boromir));
		assertAttachedTo(shield, boromir);
		assertTrue(scn.FreepsActionAvailable(shield));
		scn.FreepsUseCardAction(shield);
		assertInDiscard(shield);

		// 7 against 12: Boromir loses, and takes neither the wound nor the damage bonus.
		scn.PassCurrentPhaseActions();
		assertEquals(0, scn.GetWoundsOn(boromir));

		// Attea is fierce: in the next skirmish phase Boromir takes his wounds.
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(boromir, attea);
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(0, scn.GetWoundsOn(boromir));
		scn.PassCurrentPhaseActions();
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheShieldCannotBeDiscardedForNoWoundsWhenTheBearerIsNotBoromir() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shield = scn.GetFreepsCard("shield");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var attea = scn.GetShadowCard("attea");
		scn.MoveCompanionsToTable(boromir, aragorn);
		scn.AttachCardsTo(aragorn, shield);

		scn.StartGame();
		scn.MoveMinionsToTable(attea);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(aragorn, attea);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(shield));
		assertAttachedTo(shield, aragorn);
	}
}
