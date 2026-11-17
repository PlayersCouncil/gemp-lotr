package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_080_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("axe1", "15_80");
					put("axe2", "15_80");       // a second copy: not unique
					put("corps", "11_73");      // Corps of Harad: [men] Man minion, strength 10, fierce only at a battleground (none on this path)
					put("horde", "11_85");      // Horde of Harad: a second [men] Man title, the same text, strength 13
					put("ted", "18_75");        // Ted Sandyman: a [men] minion who is a Hobbit, no Man
					put("brigand", "4_10");     // Dunlending Brigand: a [dunland] Man minion with no text
					put("prowler", "15_61");    // Gondorian Prowler: [gondor] Man companion, hunter 1
					put("gimli", "15_5");       // Gimli, Eager Hunter: [dwarven] Dwarf companion, hunter 3
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion who is no hunter
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GreatAxeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Great Axe
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: 2
		 * Game Text: Bearer must be a [men] Man.<br>While bearer is skirmishing a hunter companion, bearer is strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("axe1");

		assertEquals("Great Axe", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void BearerMustBeAMenMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetShadowCard("axe1");
		var corps = scn.GetShadowCard("corps");
		var horde = scn.GetShadowCard("horde");
		var ted = scn.GetShadowCard("ted");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToHand(axe);

		scn.StartGame();
		assertInHand(axe);
		scn.MoveMinionsToTable(corps, horde, ted, brigand);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(axe));
		scn.ShadowPlayCard(axe);

		// Two [men] Men are offered; a [men] Hobbit and a [dunland] Man are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(corps, horde));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ted, brigand));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(horde);

		assertAttachedTo(axe, horde);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void BearerIsStrengthPlus2WhileSkirmishingAHunterCompanionAndNotBeforeTheSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetShadowCard("axe1");
		var corps = scn.GetShadowCard("corps");
		var horde = scn.GetShadowCard("horde");
		var prowler = scn.GetFreepsCard("prowler");
		scn.MoveCompanionsToTable(prowler);

		scn.StartGame();
		scn.MoveMinionsToTable(corps, horde);
		scn.AttachCardsTo(corps, axe);
		assertAttachedTo(axe, corps);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(prowler, corps);
		scn.ShadowAssignToMinions(prowler, horde);

		// Assigned to the hunter, his skirmish not begun: the printed +2 alone.
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(12, scn.GetStrength(corps));
		assertEquals(13, scn.GetStrength(horde));

		scn.FreepsResolveSkirmish(prowler);

		// Skirmishing a hunter companion: 10 + 2 printed + 2; the Horde, in the same skirmish with no Axe, is not touched.
		assertEquals(14, scn.GetStrength(corps));
		assertEquals(13, scn.GetStrength(horde));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void BearerSkirmishingACompanionWhoIsNoHunterIsNotStrengthened() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe1 = scn.GetShadowCard("axe1");
		var axe2 = scn.GetShadowCard("axe2");
		var corps = scn.GetShadowCard("corps");
		var horde = scn.GetShadowCard("horde");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(corps, horde);
		scn.AttachCardsTo(corps, axe1);
		scn.AttachCardsTo(horde, axe2);
		assertAttachedTo(axe1, corps);
		assertAttachedTo(axe2, horde);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, corps}, new PhysicalCardImpl[]{gimli, horde});
		scn.FreepsResolveSkirmish(aragorn);

		// Skirmishing Aragorn, no hunter: 10 + 2 printed. The other bearer, waiting for Gimli's skirmish: 13 + 2.
		assertEquals(12, scn.GetStrength(corps));
		assertEquals(15, scn.GetStrength(horde));
		scn.PassSkirmishActions();

		scn.FreepsResolveSkirmish(gimli);

		// Skirmishing Gimli, a hunter Dwarf: 13 + 2 printed + 2.
		assertEquals(17, scn.GetStrength(horde));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
