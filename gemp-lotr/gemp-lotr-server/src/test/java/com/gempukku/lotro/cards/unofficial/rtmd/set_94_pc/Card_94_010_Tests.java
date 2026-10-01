package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_94_010_Tests {

	private final HashMap<String, String> cards = new HashMap<>() {{
		put("aragorn", "1_89"); // Aragorn, Ranger of the North: companion to open a 2nd skirmish (vs backstabber)
		put("urukFighter", "1_146"); // Uruk Fighter: damage+1, twilight 3 - the "damaged" cost target
		put("butcher", "6_60"); // Berserk Butcher: damage+1, twilight 6 - 2nd valid cost target, and the
		// sacrificial skirmish opponent (a skirmish must be assigned or the engine skips the skirmish phase)
		put("backstabber", "1_174"); // Goblin Backstabber: no damage keyword - invalid cost target, valid
		// effect target, and the 2nd skirmish's opponent (fights the Ring-bearer)
	}};

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "94_10"
		);
	}

	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(cards,
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_10", null
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var mod = scn.GetShadowCard("mod");

		/**
		 * Set: RTMD 94
		 * Name: Race Text 94_10
		 * Type: MetaSite
		 * Game Text: Skirmish: Make your minion damage -X until the regroup phase to make another
		 * minion strength +X until the regroup phase.
		 */

		assertEquals("Race Text 94_10", mod.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, mod.getBlueprint().getCardType());
		assertEquals(-1, mod.getBlueprint().getIntensity());
	}

	@Test
	public void TradesDamageForStrengthUntilRegroup() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var mod = scn.GetShadowCard("mod");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		var urukFighter = scn.GetShadowCard("urukFighter");
		var butcher = scn.GetShadowCard("butcher");
		var backstabber = scn.GetShadowCard("backstabber");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(urukFighter, butcher, backstabber);
		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{frodo, backstabber},
				new PhysicalCardImpl[]{aragorn, butcher}
		);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(1, scn.GetKeywordCount(urukFighter, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(butcher, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(backstabber, Keyword.DAMAGE));
		int backstabberStr = scn.GetStrength(backstabber);

		assertTrue(scn.ShadowActionAvailable(mod));
		scn.ShadowUseCardAction(mod);

		// Cost filter: only damage-carrying minions are offered
		assertTrue(scn.ShadowHasCardChoiceAvailable(urukFighter));
		assertTrue(scn.ShadowHasCardChoiceAvailable(butcher));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(backstabber));
		scn.ShadowChooseCard(urukFighter);

		// X is bounded by the chosen minion's actual damage bonus
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowDecided(1);

		// Effect target: any other minion (butcher or backstabber), not the damaged one
		assertTrue(scn.ShadowHasCardChoiceAvailable(butcher));
		assertTrue(scn.ShadowHasCardChoiceAvailable(backstabber));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(urukFighter));
		scn.ShadowChooseCard(backstabber);

		assertEquals(0, scn.GetKeywordCount(urukFighter, Keyword.DAMAGE));
		assertEquals(backstabberStr + 1, scn.GetStrength(backstabber));

		// Conclude both skirmishes (aragorn/butcher, then frodo/backstabber) before checking
		// expiration - with nothing else pending in either, the engine cascades straight through
		// both - so this crosses an actual 2nd skirmish first, rather than going directly from the
		// skirmish the mod was used in to regroup with no intervening skirmish at all
		scn.FreepsPass();
		scn.ShadowPass();
		scn.FreepsResolveSkirmish(frodo);

		assertEquals(0, scn.GetKeywordCount(urukFighter, Keyword.DAMAGE));
		assertEquals(backstabberStr + 1, scn.GetStrength(backstabber));
		scn.BothPass();
		scn.FreepsDeclineOptionalTrigger(); //Ring

		// Duration: both modifications expire at regroup
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertEquals(1, scn.GetKeywordCount(urukFighter, Keyword.DAMAGE));
		assertEquals(backstabberStr, scn.GetStrength(backstabber));
	}

	@Test
	public void UnavailableWithoutADamagedMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var mod = scn.GetShadowCard("mod");
		var aragorn = scn.GetFreepsCard("aragorn");
		var backstabber = scn.GetShadowCard("backstabber");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(backstabber);
		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, backstabber);
		scn.FreepsPass();

		assertFalse(scn.ShadowActionAvailable(mod));
	}

	@Test
	public void OwnerGatingShadowCantUseWhenFreepsOwnsTheMod() throws DecisionResultInvalidException, CardNotFoundException {
		// 94_10's ability says "your minion" - a Shadow minion is never "your" for a Freeps-owned
		// mod, so the ability must be absent for the non-owner (Shadow).
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		var aragorn = scn.GetFreepsCard("aragorn");
		var urukFighter = scn.GetShadowCard("urukFighter");
		var backstabber = scn.GetShadowCard("backstabber");

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(urukFighter, backstabber);
		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, urukFighter);
		scn.FreepsPass();

		assertFalse(scn.ShadowActionAvailable(mod));
	}
}
