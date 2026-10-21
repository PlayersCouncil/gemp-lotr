package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_038_Tests

{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shield", "2_38");

					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man companion, strength 7, vitality 3; a skirmish ability for Hobbits
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] companion who is not Boromir; a maneuver ability only used on purpose
					put("legolas", "1_50");     // Legolas, Greenleaf: [elven] archer companion, not [gondor]; an archery ability only used on purpose
					put("merchant", "7_97");    // Gondorian Merchant: the only [gondor] ally, so [gondor] but not a companion
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion; his skirmish ability is used only on purpose

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
					// Ted Sandyman, Chief's Men's Ally: a [men] Hobbit minion, strength 3; his abilities are used only on purpose
					put("ted", "18_75");
					// Long Battle Bow: ranged weapon, strength +1, on a [men] minion; makes bearer an archer; its optional
					// trigger on being played does not fire when it is cheated on
					put("bow", "11_89");
					// The Balrog, The Terror of Khazad-dum: strength 17, damage +1; its texts act only at an underground site
					put("balrog", "12_79");
					// The Balrog's Sword: an artifact hand weapon, strength +3, on The Balrog; "It is damage +1"
					put("balrogsword", "2_50");
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
		assertEquals(1, card.getBlueprint().getPossessionClasses().size());
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
		var pippin = scn.GetFreepsCard("pippin");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(boromir, aragorn, legolas, pippin);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(shield);
		assertInHand(shield);

		scn.StartGame();
		scn.MoveMinionsToTable("marksman", "marksman2");
		scn.FreepsRefreshDecision();

		assertEquals(2, scn.GetShadowArcheryTotal());
		assertEquals(1, scn.GetFreepsArcheryTotal());
		assertTrue(scn.FreepsPlayAvailable(shield));

		scn.FreepsPlayCard(shield);
		// [gondor] companions: two Men and a Hobbit; not an [elven] companion, the [gondor] ally or the [shire] Ring-bearer.
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, aragorn, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(legolas, merchant, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		// Not Boromir: the archery total does not depend on who bears it.
		scn.FreepsChooseCard(aragorn);

		assertAttachedTo(shield, aragorn);
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
		var ted = scn.GetShadowCard("ted");
		var balrog = scn.GetShadowCard("balrog");
		var axe = scn.GetShadowCard("axe");
		var axe2 = scn.GetShadowCard("axe2");
		var scimitar = scn.GetShadowCard("scimitar");
		var warg = scn.GetShadowCard("warg");
		var bow = scn.GetShadowCard("bow");
		var balrogsword = scn.GetShadowCard("balrogsword");
		scn.MoveCompanionsToTable(boromir, aragorn);
		scn.AttachCardsTo(boromir, shield);
		assertAttachedTo(shield, boromir);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand, brigand2, runner, denizen, ted, balrog);
		scn.AttachCardsTo(brigand, axe);
		scn.AttachCardsTo(brigand2, axe2);
		scn.AttachCardsTo(runner, scimitar);
		scn.AttachCardsTo(denizen, warg);
		// The bow makes Ted an archer: 1 to the minion archery total, which the shield takes off again.
		scn.AttachCardsTo(ted, bow);
		scn.AttachCardsTo(balrog, balrogsword);
		assertAttachedTo(axe, brigand);
		assertAttachedTo(axe2, brigand2);
		assertAttachedTo(scimitar, runner);
		assertAttachedTo(warg, denizen);
		assertAttachedTo(bow, ted);
		assertAttachedTo(balrogsword, balrog);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		// 8 + 3 from the axe; 5 + 2 from the scimitar; 7 + 4 from the warg; 3 + 1 from the bow; 17 + 3 from the sword
		assertEquals(11, scn.GetStrength(brigand));
		assertEquals(11, scn.GetStrength(brigand2));
		assertEquals(7, scn.GetStrength(runner));
		assertEquals(11, scn.GetStrength(denizen));
		assertEquals(4, scn.GetStrength(ted));
		assertEquals(20, scn.GetStrength(balrog));
		// The Balrog's own damage +1 and the sword's
		assertEquals(2, scn.GetKeywordCount(balrog, Keyword.DAMAGE));

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{boromir, brigand},
				new PhysicalCardImpl[]{aragorn, brigand2});
		// The Free Peoples player assigns one minion to each; the Shadow player adds the other four to Boromir.
		scn.ShadowAssignToMinions(boromir, runner, denizen, ted, balrog);

		// Assigned to Boromir, and his skirmish not yet begun: every bonus in force.
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
		assertEquals(11, scn.GetStrength(brigand));
		assertEquals(20, scn.GetStrength(balrog));

		// Aragorn's skirmish first, while Boromir bears the Shield and waits for his own: a minion skirmishing
		// another companion keeps its weapon bonus.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, brigand2));
		assertEquals(11, scn.GetStrength(brigand2));
		assertEquals(11, scn.GetStrength(brigand));

		// Aragorn loses, 8 to 11, and takes a wound.
		scn.PassCurrentPhaseActions();
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
		scn.FreepsResolveSkirmish(boromir);

		// Hand weapons of two cultures, a ranged weapon and an artifact hand weapon add nothing.
		assertTrue(scn.IsCharSkirmishingAgainst(boromir, brigand));
		assertEquals(8, scn.GetStrength(brigand));
		assertEquals(5, scn.GetStrength(runner));
		assertEquals(3, scn.GetStrength(ted));
		assertEquals(17, scn.GetStrength(balrog));
		// Damage bonuses are not strength bonuses, and stay.
		assertEquals(2, scn.GetKeywordCount(balrog, Keyword.DAMAGE));
		// A mount is not a weapon.
		assertEquals(11, scn.GetStrength(denizen));
		// Assigned to Aragorn, whose skirmish is over.
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
		var axe = scn.GetShadowCard("axe");
		var axe2 = scn.GetShadowCard("axe2");
		scn.MoveCompanionsToTable(boromir, aragorn);
		scn.AttachCardsTo(aragorn, shield);
		assertAttachedTo(shield, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand, brigand2);
		scn.AttachCardsTo(brigand, axe);
		scn.AttachCardsTo(brigand2, axe2);
		assertAttachedTo(axe, brigand);
		assertAttachedTo(axe2, brigand2);

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
}
