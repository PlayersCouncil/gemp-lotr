package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_229_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eowyn", "7_229");
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion, strength 6, valiant and nothing else

					put("chain", "4_278");      // Heavy Chain: [rohan] possession (armor), bearer must be a Man; an overwhelm bar
					put("sword", "4_291");      // Sword of Rohan: [rohan] hand weapon for a [rohan] Man, strength +2; damage +1 against an Uruk-hai
					// Spider Poison: a [gollum] possession, a Shadow card a companion can bear; its bearer exerts each time he
					// is assigned to a skirmish.
					put("poison", "8_28");
					// Protecting the Hall: a [rohan] condition, bearer must be a [rohan] Man; an assignment ability used only
					// on purpose.
					put("hall", "11_149");

					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai, strength 5, vitality 3; damage +1 is its whole text
					put("chieftain", "1_266");  // Orc Chieftain: a [sauron] Orc, strength 7; its text answers a weapon played on it, never here
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EowynStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Éowyn, Restless Maiden
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Aragorn
		 * Game Text: Skirmish: Discard a possession borne by Éowyn to make her strength +2.
		 */

		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");

		assertEquals("Éowyn", eowyn.getBlueprint().getTitle());
		assertEquals("Restless Maiden", eowyn.getBlueprint().getSubtitle());
		assertTrue(eowyn.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, eowyn.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, eowyn.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, eowyn.getBlueprint().getCardType());
		assertEquals(Race.MAN, eowyn.getBlueprint().getRace());
		assertEquals(2, eowyn.getBlueprint().getTwilightCost());
		assertEquals(6, eowyn.getBlueprint().getStrength());
		assertEquals(3, eowyn.getBlueprint().getVitality());
		assertEquals(6, eowyn.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, eowyn.getBlueprint().getSignet());
	}

	@Test
	public void SkirmishAbilityDiscardsAPossessionSheBearsToMakeHerStrengthPlus2UntilTheSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");
		var guard = scn.GetFreepsCard("guard");
		var chain = scn.GetFreepsCard("chain");
		var sword = scn.GetFreepsCard("sword");
		var hall = scn.GetFreepsCard("hall");
		var poison = scn.GetShadowCard("poison");
		var savage = scn.GetShadowCard("savage");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCompanionsToTable(eowyn, guard);
		scn.AttachCardsTo(eowyn, chain, hall, poison);
		scn.AttachCardsTo(guard, sword);
		assertAttachedTo(chain, eowyn);
		assertAttachedTo(hall, eowyn);
		assertAttachedTo(poison, eowyn);
		assertAttachedTo(sword, guard);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, chieftain);
		assertEquals(0, scn.GetWoundsOn(eowyn));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { eowyn, savage },
				new PhysicalCardImpl[] { guard, chieftain }
		);
		// Spider Poison's own text: its bearer exerts as she is assigned.
		assertEquals(1, scn.GetWoundsOn(eowyn));
		scn.FreepsResolveSkirmish(eowyn);

		assertEquals(6, scn.GetStrength(eowyn));
		assertEquals(8, scn.GetStrength(guard));    // 6 + 2 for the Sword
		assertTrue(scn.FreepsActionAvailable(eowyn));

		scn.FreepsUseCardAction(eowyn);

		// A possession she bears, of either side; not a condition she bears, nor a possession another companion bears.
		assertTrue(scn.FreepsHasCardChoiceAvailable(chain, poison));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(hall, sword));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(chain);

		assertInDiscard(chain);
		assertEquals(8, scn.GetStrength(eowyn));
		// Herself alone: the other companion is untouched.
		assertEquals(8, scn.GetStrength(guard));
		assertAttachedTo(poison, eowyn);
		assertAttachedTo(hall, eowyn);
		assertAttachedTo(sword, guard);

		// Again: the Spider Poison is the only possession she bears now, so it goes unasked.
		scn.ShadowPass();
		scn.FreepsUseCardAction(eowyn);

		assertInDiscard(poison);
		assertEquals(10, scn.GetStrength(eowyn));

		// With only a condition left on her, the cost cannot be paid.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertAttachedTo(hall, eowyn);
		assertFalse(scn.FreepsActionAvailable(eowyn));

		// Her 10 overwhelms the Savage's 5; in the next skirmish her bonus is gone.
		scn.PassSkirmishActions();
		assertInDiscard(savage);
		scn.FreepsResolveSkirmish(guard);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(6, scn.GetStrength(eowyn));
	}

	@Test
	public void SkirmishAbilityIsOfferedWhileSheIsInNoSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");
		var guard = scn.GetFreepsCard("guard");
		var chain = scn.GetFreepsCard("chain");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCompanionsToTable(eowyn, guard);
		scn.AttachCardsTo(eowyn, chain);
		assertAttachedTo(chain, eowyn);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, chieftain);
		scn.FreepsResolveSkirmish(guard);

		// The Guard's skirmish; Éowyn is in none, and her text names no skirmish.
		assertFalse(scn.IsCharSkirmishing(eowyn));
		assertEquals(6, scn.GetStrength(eowyn));
		assertTrue(scn.FreepsActionAvailable(eowyn));

		scn.FreepsUseCardAction(eowyn);

		assertInDiscard(chain);
		assertEquals(8, scn.GetStrength(eowyn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
